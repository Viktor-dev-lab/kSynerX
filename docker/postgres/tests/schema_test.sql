-- Run only in the disposable database created by verify.sh.
-- Any failed assertion stops psql with a non-zero exit code.
BEGIN;

CREATE FUNCTION pg_temp.expect_error(statement text, expected_state text, expected_constraint text DEFAULT NULL)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    actual_constraint text;
BEGIN
    BEGIN
        EXECUTE statement;
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS actual_constraint = CONSTRAINT_NAME;
        IF SQLSTATE <> expected_state THEN
            RAISE EXCEPTION 'Expected SQLSTATE %, got %: %', expected_state, SQLSTATE, SQLERRM;
        END IF;
        IF expected_constraint IS NOT NULL AND actual_constraint <> expected_constraint THEN
            RAISE EXCEPTION 'Expected constraint %, got %', expected_constraint, actual_constraint;
        END IF;
        RETURN;
    END;
    RAISE EXCEPTION 'Expected SQLSTATE %, but statement succeeded: %', expected_state, statement;
END;
$$;

DO $$
DECLARE
    table_name text;
    parent_column text;
    parent_id bigint;
    product_row_id bigint;
    bundle_row_id bigint;
    channel_row_id bigint;
    event_row_id bigint;
    row_count bigint;
BEGIN
    IF (SELECT count(*) FROM information_schema.tables
        WHERE table_schema = 'public' AND table_type = 'BASE TABLE') <> 8 THEN
        RAISE EXCEPTION 'Expected exactly 8 CDMS tables';
    END IF;
    IF enum_range(NULL::channel_type)::text <> '{POLLING,WEBHOOK,EXCEL}'
       OR enum_range(NULL::inbox_status)::text <> '{PENDING,RETRY,APPLIED,STALE,DUPLICATE,CONFLICT,FAILED}' THEN
        RAISE EXCEPTION 'Enum values differ from the DBML';
    END IF;
    IF (SELECT count(*) FROM pg_indexes WHERE schemaname = 'public' AND indexname IN (
        'uq_products_source_product', 'idx_products_source_sku', 'idx_products_source_partner_sku',
        'uq_product_units_position', 'uq_product_categories_position', 'uq_product_bundles_position',
        'uq_product_bundle_details_position', 'uq_product_barcodes_position',
        'idx_inbox_worker_pick', 'idx_inbox_channel_product'
    )) <> 10 THEN
        RAISE EXCEPTION 'Missing named indexes from the DBML';
    END IF;
    IF (SELECT count(*) FROM pg_constraint
        WHERE connamespace = 'public'::regnamespace AND contype = 'f') <> 6 THEN
        RAISE EXCEPTION 'Expected exactly 6 foreign keys';
    END IF;

    INSERT INTO products (source_code, product_id, revision, content_hash, parent_partner_sku)
    VALUES ('TEST_SOURCE', 101, 1, repeat('a', 64), 'PARENT_NOT_INGESTED')
    RETURNING id INTO product_row_id;
    IF product_row_id <= 0 OR NOT EXISTS (
        SELECT 1 FROM products WHERE id = product_row_id AND created_at = now() AND updated_at = now()
    ) THEN
        RAISE EXCEPTION 'Product identity/timestamp defaults failed';
    END IF;
    -- Same external ID is allowed for a different logical source.
    INSERT INTO products (source_code, product_id, revision, content_hash)
    VALUES ('OTHER_SOURCE', 101, 1, repeat('b', 64));
    PERFORM pg_temp.expect_error(
        $q$INSERT INTO products (source_code, product_id, revision, content_hash)
           VALUES ('TEST_SOURCE', 101, 2, repeat('b', 64))$q$,
        '23505', 'uq_products_source_product');
    PERFORM pg_temp.expect_error('UPDATE products SET revision = 0', '23514', 'chk_products_revision');
    PERFORM pg_temp.expect_error('UPDATE products SET revision = NULL', '23502');
    PERFORM pg_temp.expect_error($q$UPDATE products SET content_hash = repeat('A', 64)$q$,
        '23514', 'chk_products_content_hash');
    PERFORM pg_temp.expect_error($q$UPDATE products SET content_hash = 'abc'$q$,
        '23514', 'chk_products_content_hash');

    -- Every child accepts zero-based ordered rows, but not duplicates, negative positions or orphans.
    FOREACH table_name IN ARRAY ARRAY['product_units', 'product_categories', 'product_bundles',
                                      'product_bundle_details', 'product_barcodes'] LOOP
        parent_column := CASE WHEN table_name = 'product_bundle_details'
                              THEN 'product_bundle_id' ELSE 'product_record_id' END;
        parent_id := CASE WHEN table_name = 'product_bundle_details'
                          THEN bundle_row_id ELSE product_row_id END;
        EXECUTE format('INSERT INTO %I (%I, position) VALUES ($1, 0)', table_name, parent_column)
            USING parent_id;
        EXECUTE format('INSERT INTO %I (%I, position) VALUES ($1, 1)', table_name, parent_column)
            USING parent_id;
        PERFORM pg_temp.expect_error(format('INSERT INTO %I (%I, position) VALUES (%s, 0)',
            table_name, parent_column, parent_id), '23505', 'uq_' || table_name || '_position');
        PERFORM pg_temp.expect_error(format('INSERT INTO %I (%I, position) VALUES (%s, -1)',
            table_name, parent_column, parent_id), '23514', 'chk_' || table_name || '_position');
        PERFORM pg_temp.expect_error(format('INSERT INTO %I (%I, position) VALUES (-1, 0)',
            table_name, parent_column), '23503');
        IF table_name = 'product_bundles' THEN
            SELECT id INTO bundle_row_id FROM product_bundles
            WHERE product_record_id = product_row_id AND position = 0;
        END IF;
    END LOOP;
    -- External barcode product ID is deliberately not a foreign key.
    UPDATE product_barcodes SET barcode_product_id = 999999;

    INSERT INTO channels (type, poll_interval_seconds) VALUES ('POLLING', 60)
    RETURNING id INTO channel_row_id;
    INSERT INTO channels (type) VALUES ('WEBHOOK'), ('EXCEL');
    IF NOT EXISTS (SELECT 1 FROM channels WHERE id = channel_row_id
                   AND source_code = 'VIETFUL_DEMO' AND enabled AND created_at = now()) THEN
        RAISE EXCEPTION 'Channel defaults failed';
    END IF;
    PERFORM pg_temp.expect_error($q$INSERT INTO channels (type) VALUES ('POLLING')$q$, '23505');
    PERFORM pg_temp.expect_error($q$INSERT INTO channels (type) VALUES ('INVALID')$q$, '22P02');
    PERFORM pg_temp.expect_error('UPDATE channels SET poll_interval_seconds = 0',
        '23514', 'chk_channels_poll_interval');

    -- Inbox delivery may precede product ingestion; duplicate deliveries are allowed.
    INSERT INTO inbox_events (channel_id, product_id, revision, payload, content_hash)
    VALUES (channel_row_id, 999999, 1, '{"productId":999999}', repeat('c', 64))
    RETURNING id INTO event_row_id;
    INSERT INTO inbox_events (channel_id, product_id, revision, payload, content_hash)
    SELECT channel_id, product_id, revision, payload, content_hash FROM inbox_events WHERE id = event_row_id;
    IF (SELECT count(*) FROM inbox_events) <> 2 OR NOT EXISTS (
        SELECT 1 FROM inbox_events WHERE id = event_row_id AND status = 'PENDING'
            AND retry_count = 0 AND next_retry_at = now() AND created_at = now() AND processed_at IS NULL
    ) THEN
        RAISE EXCEPTION 'Inbox defaults/duplicate delivery behavior failed';
    END IF;
    PERFORM pg_temp.expect_error('UPDATE inbox_events SET channel_id = -1', '23503');
    PERFORM pg_temp.expect_error('UPDATE inbox_events SET revision = 0', '23514', 'chk_inbox_revision');
    PERFORM pg_temp.expect_error('UPDATE inbox_events SET retry_count = -1', '23514', 'chk_inbox_retry_count');
    PERFORM pg_temp.expect_error($q$UPDATE inbox_events SET payload = '[]'$q$, '23514', 'chk_inbox_payload_object');
    PERFORM pg_temp.expect_error($q$UPDATE inbox_events SET payload = 'null'$q$, '23514', 'chk_inbox_payload_object');
    PERFORM pg_temp.expect_error('UPDATE inbox_events SET payload = NULL', '23502');
    PERFORM pg_temp.expect_error($q$UPDATE inbox_events SET content_hash = repeat('g', 64)$q$,
        '23514', 'chk_inbox_content_hash');
    PERFORM pg_temp.expect_error($q$UPDATE inbox_events SET content_hash = 'abc'$q$,
        '23514', 'chk_inbox_content_hash');
    PERFORM pg_temp.expect_error($q$UPDATE inbox_events SET status = 'PROCESSING'$q$, '22P02');
    PERFORM pg_temp.expect_error(format('DELETE FROM channels WHERE id = %s', channel_row_id),
        '23503', 'fk_inbox_events_channel');

    DELETE FROM products WHERE id = product_row_id;
    FOREACH table_name IN ARRAY ARRAY['product_units', 'product_categories', 'product_bundles',
                                      'product_bundle_details', 'product_barcodes'] LOOP
        EXECUTE format('SELECT count(*) FROM %I', table_name) INTO row_count;
        IF row_count <> 0 THEN
            RAISE EXCEPTION 'Cascade deletion did not reach %', table_name;
        END IF;
    END LOOP;
    IF (SELECT count(*) FROM inbox_events) <> 2 THEN
        RAISE EXCEPTION 'Product deletion must not remove inbox events';
    END IF;

    RAISE NOTICE 'CDMS schema checks passed: tables, enums, indexes, defaults, unique/check/FK constraints, cascades and inbox delivery behavior.';
END;
$$;

ROLLBACK;
