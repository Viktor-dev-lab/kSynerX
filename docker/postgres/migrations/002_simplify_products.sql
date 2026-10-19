BEGIN;

SET LOCAL search_path TO public;
LOCK TABLE products, product_categories IN ACCESS EXCLUSIVE MODE;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'products' AND column_name = 'is_expiry_date'
    ) THEN
        ALTER TABLE products RENAME COLUMN is_expiry_date TO has_expiration;
    END IF;
END;
$$;

ALTER TABLE products
    DROP COLUMN IF EXISTS outbound_type,
    DROP COLUMN IF EXISTS is_attribute,
    DROP COLUMN IF EXISTS is_lot_number,
    DROP COLUMN IF EXISTS is_tracking_serial,
    DROP COLUMN IF EXISTS num_days_warning_exp_date,
    DROP COLUMN IF EXISTS num_days_export_before_exp_date,
    DROP COLUMN IF EXISTS packing_note,
    DROP COLUMN IF EXISTS avatar_url,
    DROP COLUMN IF EXISTS is_physical,
    DROP COLUMN IF EXISTS is_discreet,
    DROP COLUMN IF EXISTS parent_partner_sku,
    DROP COLUMN IF EXISTS hs_code,
    DROP COLUMN IF EXISTS country_of_origin,
    DROP COLUMN IF EXISTS ref_code;

DROP TABLE IF EXISTS product_bundle_details;
DROP TABLE IF EXISTS product_bundles;
DROP TABLE IF EXISTS product_units;
DROP TABLE IF EXISTS product_barcodes;

COMMENT ON TABLE products IS
    'Latest simplified product snapshot per logical source; categories are stored in product_categories.';
COMMENT ON COLUMN products.has_expiration IS 'Business hasExpiration flag.';
COMMENT ON COLUMN products.content_hash IS
    'SHA-256 of normalized business content including ordered categories; excludes revision and local metadata.';
COMMENT ON COLUMN products.revision IS 'Source revision supplied by ingestion.';
COMMENT ON COLUMN products.id IS 'Internal database ID referenced by categories.';
COMMENT ON COLUMN inbox_events.payload IS 'Complete normalized simplified product snapshot.';
COMMENT ON TABLE inbox_events IS
    'Select eligible PENDING/RETRY events FOR UPDATE SKIP LOCKED. Commit product, categories and inbox outcome in one transaction. Record retries after rollback.';

COMMIT;
