BEGIN;

DO $$
DECLARE
    legacy_user_id BIGINT;
    target_user_id BIGINT;
    real_user_count INTEGER;
BEGIN
    SELECT id INTO legacy_user_id FROM app_users WHERE is_legacy_owner;
    IF legacy_user_id IS NULL THEN
        RAISE EXCEPTION 'No legacy owner exists; claim is not required or was already completed';
    END IF;

    SELECT COUNT(*), MIN(id) INTO real_user_count, target_user_id
    FROM app_users WHERE NOT is_legacy_owner;
    IF real_user_count <> 1 THEN
        RAISE EXCEPTION 'Legacy claim requires exactly one real user, found %', real_user_count;
    END IF;

    UPDATE daily_records SET owner_user_id = target_user_id WHERE owner_user_id = legacy_user_id;
    UPDATE body_weight_records SET owner_user_id = target_user_id WHERE owner_user_id = legacy_user_id;
    UPDATE body_circumference_records SET owner_user_id = target_user_id WHERE owner_user_id = legacy_user_id;
    UPDATE food_items SET owner_user_id = target_user_id
        WHERE owner_user_id = legacy_user_id AND is_custom = 1;

    IF EXISTS (
        SELECT 1 FROM daily_records WHERE owner_user_id = legacy_user_id
        UNION ALL SELECT 1 FROM body_weight_records WHERE owner_user_id = legacy_user_id
        UNION ALL SELECT 1 FROM body_circumference_records WHERE owner_user_id = legacy_user_id
        UNION ALL SELECT 1 FROM food_items WHERE owner_user_id = legacy_user_id
    ) THEN
        RAISE EXCEPTION 'Legacy-owned rows remain after claim';
    END IF;

    DELETE FROM app_users WHERE id = legacy_user_id AND is_legacy_owner;
END $$;

COMMIT;
