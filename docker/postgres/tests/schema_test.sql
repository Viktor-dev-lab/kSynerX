-- Run only in the disposable database created by verify.sh.
BEGIN;

CREATE FUNCTION pg_temp.expect_error(statement text, expected_state text)
RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    BEGIN
        EXECUTE statement;
    EXCEPTION WHEN OTHERS THEN
        IF SQLSTATE <> expected_state THEN
            RAISE EXCEPTION 'Expected SQLSTATE %, got %: %', expected_state, SQLSTATE, SQLERRM;
        END IF;
        RETURN;
    END;
    RAISE EXCEPTION 'Expected SQLSTATE %, but statement succeeded: %', expected_state, statement;
END;
$$;

DO $$
DECLARE
    product_row_id bigint;
    channel_row_id bigint;
BEGIN
    IF (SELECT count(*) FROM information_schema.tables
        WHERE table_schema = current_schema() AND table_type = 'BASE TABLE') <> 4 THEN
        RAISE EXCEPTION 'Expected 4 CDMS tables';
    END IF;
    IF (SELECT count(*) FROM information_schema.columns
        WHERE table_schema = current_schema() AND table_name = 'products') <> 17 THEN
        RAISE EXCEPTION 'Unexpected product columns';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema() AND table_name = 'products' AND column_name = 'has_expiration') THEN
        RAISE EXCEPTION 'Missing has_expiration';
    END IF;
    IF (SELECT count(*) FROM pg_constraint
        WHERE connamespace = current_schema()::regnamespace AND contype = 'f') <> 2 THEN
        RAISE EXCEPTION 'Expected 2 foreign keys';
    END IF;
    IF enum_range(NULL::channel_type)::text <> '{POLLING,WEBHOOK,EXCEL}'
       OR enum_range(NULL::inbox_status)::text <> '{PENDING,RETRY,APPLIED,STALE,DUPLICATE,CONFLICT,FAILED}' THEN
        RAISE EXCEPTION 'Unexpected enum values';
    END IF;

    INSERT INTO products (source_code, product_id, product_name, has_expiration, revision, content_hash)
    VALUES ('TEST_SOURCE', 123, 'Kẽm', true, 1, repeat('a', 64))
    RETURNING id INTO product_row_id;
    INSERT INTO products (source_code, product_id, revision, content_hash)
    VALUES ('OTHER_SOURCE', 123, 1, repeat('b', 64));
    PERFORM pg_temp.expect_error(
        $q$INSERT INTO products (source_code, product_id, revision, content_hash)
           VALUES ('TEST_SOURCE', 123, 2, repeat('b', 64))$q$, '23505');
    PERFORM pg_temp.expect_error('UPDATE products SET revision = 0', '23514');
    PERFORM pg_temp.expect_error($q$UPDATE products SET content_hash = repeat('A', 64)$q$, '23514');

    INSERT INTO product_categories (product_record_id, position, category_code, category_name)
    VALUES (product_row_id, 0, 'CATEGORY001', 'Category 1'),
           (product_row_id, 1, 'CATEGORY002', 'Category 2');
    PERFORM pg_temp.expect_error(format(
        'INSERT INTO product_categories (product_record_id, position) VALUES (%s, 0)', product_row_id), '23505');
    PERFORM pg_temp.expect_error(format(
        'INSERT INTO product_categories (product_record_id, position) VALUES (%s, -1)', product_row_id), '23514');
    PERFORM pg_temp.expect_error(
        'INSERT INTO product_categories (product_record_id, position) VALUES (-1, 0)', '23503');
    DELETE FROM products WHERE id = product_row_id;
    IF EXISTS (SELECT 1 FROM product_categories WHERE product_record_id = product_row_id) THEN
        RAISE EXCEPTION 'Category cascade failed';
    END IF;

    INSERT INTO channels (type, poll_interval_seconds) VALUES ('POLLING', 30)
    RETURNING id INTO channel_row_id;
    INSERT INTO inbox_events (channel_id, product_id, revision, payload, content_hash)
    VALUES (channel_row_id, 123, 1, '{"productId":123,"categories":[]}', repeat('a', 64)),
           (channel_row_id, 123, 1, '{"productId":123,"categories":[]}', repeat('a', 64));
    PERFORM pg_temp.expect_error(format('DELETE FROM channels WHERE id = %s', channel_row_id), '23503');
    PERFORM pg_temp.expect_error($q$UPDATE inbox_events SET payload = '[]'::jsonb$q$, '23514');
    PERFORM pg_temp.expect_error('UPDATE inbox_events SET revision = 0', '23514');
    PERFORM pg_temp.expect_error('UPDATE inbox_events SET retry_count = -1', '23514');
    DELETE FROM inbox_events;
    DELETE FROM channels WHERE id = channel_row_id;
END;
$$;

ROLLBACK;
