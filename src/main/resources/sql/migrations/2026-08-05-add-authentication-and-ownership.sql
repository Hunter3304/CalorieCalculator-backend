BEGIN;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    wechat_openid VARCHAR(64) UNIQUE,
    is_legacy_owner BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT app_users_identity_check CHECK (
        (is_legacy_owner AND wechat_openid IS NULL)
        OR (NOT is_legacy_owner AND wechat_openid IS NOT NULL)
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS app_users_single_legacy_owner
    ON app_users (is_legacy_owner) WHERE is_legacy_owner;

CREATE TABLE IF NOT EXISTS app_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS app_sessions_user_id_idx ON app_sessions(user_id);
CREATE INDEX IF NOT EXISTS app_sessions_expires_at_idx ON app_sessions(expires_at);

ALTER TABLE food_items ADD COLUMN IF NOT EXISTS owner_user_id BIGINT;
ALTER TABLE daily_records ADD COLUMN IF NOT EXISTS owner_user_id BIGINT;
ALTER TABLE body_weight_records ADD COLUMN IF NOT EXISTS owner_user_id BIGINT;
ALTER TABLE body_circumference_records ADD COLUMN IF NOT EXISTS owner_user_id BIGINT;

UPDATE food_items SET is_custom = 0 WHERE is_custom IS NULL;
ALTER TABLE food_items ALTER COLUMN is_custom SET DEFAULT 0;
ALTER TABLE food_items ALTER COLUMN is_custom SET NOT NULL;

DO $$
DECLARE
    legacy_user_id BIGINT;
    legacy_rows_exist BOOLEAN;
BEGIN
    SELECT EXISTS (SELECT 1 FROM daily_records WHERE owner_user_id IS NULL)
        OR EXISTS (SELECT 1 FROM body_weight_records WHERE owner_user_id IS NULL)
        OR EXISTS (SELECT 1 FROM body_circumference_records WHERE owner_user_id IS NULL)
        OR EXISTS (SELECT 1 FROM food_items WHERE is_custom = 1 AND owner_user_id IS NULL)
    INTO legacy_rows_exist;

    IF legacy_rows_exist THEN
        INSERT INTO app_users (wechat_openid, is_legacy_owner)
        SELECT NULL, TRUE
        WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE is_legacy_owner);

        SELECT id INTO STRICT legacy_user_id FROM app_users WHERE is_legacy_owner;
        UPDATE daily_records SET owner_user_id = legacy_user_id WHERE owner_user_id IS NULL;
        UPDATE body_weight_records SET owner_user_id = legacy_user_id WHERE owner_user_id IS NULL;
        UPDATE body_circumference_records SET owner_user_id = legacy_user_id WHERE owner_user_id IS NULL;
        UPDATE food_items SET owner_user_id = legacy_user_id
            WHERE is_custom = 1 AND owner_user_id IS NULL;
    END IF;
END $$;

ALTER TABLE daily_records ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE body_weight_records ALTER COLUMN owner_user_id SET NOT NULL;
ALTER TABLE body_circumference_records ALTER COLUMN owner_user_id SET NOT NULL;

ALTER TABLE body_weight_records DROP CONSTRAINT IF EXISTS body_weight_records_record_date_key;
ALTER TABLE body_circumference_records DROP CONSTRAINT IF EXISTS body_circumference_records_record_date_key;

CREATE UNIQUE INDEX IF NOT EXISTS body_weight_owner_record_date_key
    ON body_weight_records(owner_user_id, record_date);
CREATE UNIQUE INDEX IF NOT EXISTS body_circumference_owner_record_date_key
    ON body_circumference_records(owner_user_id, record_date);
CREATE INDEX IF NOT EXISTS daily_records_owner_date_idx
    ON daily_records(owner_user_id, record_date);
CREATE INDEX IF NOT EXISTS food_items_owner_custom_idx
    ON food_items(owner_user_id, is_custom);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'daily_records_owner_fk') THEN
        ALTER TABLE daily_records ADD CONSTRAINT daily_records_owner_fk
            FOREIGN KEY (owner_user_id) REFERENCES app_users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'body_weight_records_owner_fk') THEN
        ALTER TABLE body_weight_records ADD CONSTRAINT body_weight_records_owner_fk
            FOREIGN KEY (owner_user_id) REFERENCES app_users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'body_circumference_records_owner_fk') THEN
        ALTER TABLE body_circumference_records ADD CONSTRAINT body_circumference_records_owner_fk
            FOREIGN KEY (owner_user_id) REFERENCES app_users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'food_items_owner_fk') THEN
        ALTER TABLE food_items ADD CONSTRAINT food_items_owner_fk
            FOREIGN KEY (owner_user_id) REFERENCES app_users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'food_items_custom_owner_check') THEN
        ALTER TABLE food_items ADD CONSTRAINT food_items_custom_owner_check CHECK (
            (is_custom = 1 AND owner_user_id IS NOT NULL)
            OR (is_custom = 0 AND owner_user_id IS NULL)
        );
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS user_food_usage (
    owner_user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    food_id INTEGER NOT NULL REFERENCES food_items(id) ON DELETE CASCADE,
    last_used_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (owner_user_id, food_id)
);

COMMIT;
