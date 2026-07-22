-- Non-destructive migration for existing CalorieCalculator databases.
CREATE TABLE IF NOT EXISTS body_weight_records (
    id SERIAL PRIMARY KEY,
    record_date DATE NOT NULL UNIQUE,
    weight_kg NUMERIC(8,1) NOT NULL CHECK (weight_kg > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_body_weight_records_date
    ON body_weight_records (record_date);