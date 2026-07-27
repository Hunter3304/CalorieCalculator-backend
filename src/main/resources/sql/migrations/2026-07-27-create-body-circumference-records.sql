-- Non-destructive migration for existing CalorieCalculator databases.
CREATE TABLE IF NOT EXISTS body_circumference_records (
    id SERIAL PRIMARY KEY,
    record_date DATE NOT NULL UNIQUE,
    chest_cm NUMERIC(8,1) CHECK (chest_cm > 0),
    waist_cm NUMERIC(8,1) CHECK (waist_cm > 0),
    hip_cm NUMERIC(8,1) CHECK (hip_cm > 0),
    arm_cm NUMERIC(8,1) CHECK (arm_cm > 0),
    thigh_cm NUMERIC(8,1) CHECK (thigh_cm > 0),
    calf_cm NUMERIC(8,1) CHECK (calf_cm > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT body_circumference_has_value CHECK (
        chest_cm IS NOT NULL OR waist_cm IS NOT NULL OR hip_cm IS NOT NULL
        OR arm_cm IS NOT NULL OR thigh_cm IS NOT NULL OR calf_cm IS NOT NULL
    )
);

CREATE INDEX IF NOT EXISTS idx_body_circumference_records_date
    ON body_circumference_records (record_date);
