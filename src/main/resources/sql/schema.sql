-- 如果表已存在则删除，避免冲突 / Lösche die Tabelle, falls sie existiert, um Konflikte zu vermeiden
DROP TABLE IF EXISTS food_items;

-- 创建食物字典表，基于三大营养素和中英双语
-- Erstelle die Lebensmittel-Wörterbuch-Tabelle, basierend auf den drei Makronährstoffen und Zweisprachigkeit
CREATE TABLE food_items (
    id SERIAL PRIMARY KEY,                     -- 自增主键 / Primärschlüssel (Auto-Inkrement)
    name_zh VARCHAR(100) NOT NULL,             -- 中文名称 / Chinesischer Name
    name_en VARCHAR(100) NOT NULL,             -- 英文名称 / Englischer Name
    protein_per_100g NUMERIC(5, 2) NOT NULL,   -- 每100克蛋白质(g) / Protein pro 100g (g)
    fat_per_100g NUMERIC(5, 2) NOT NULL,       -- 每100克脂肪(g) / Fett pro 100g (g)
    carbs_per_100g NUMERIC(5, 2) NOT NULL,     -- 每100克碳水化合物(g) / Kohlenhydrate pro 100g (g)
    is_custom INT DEFAULT 0,
    last_used_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 新增：置顶时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 创建时间 / Erstellungszeit
);

-- 每日饮食记录表
CREATE TABLE IF NOT EXISTS daily_records (
    id SERIAL PRIMARY KEY,
    record_date DATE NOT NULL,
    food_id INT NOT NULL,
    weight NUMERIC(8,2) NOT NULL
);
-- 每日体重记录表：每个自然日最多一条真实记录
CREATE TABLE IF NOT EXISTS body_weight_records (
    id SERIAL PRIMARY KEY,
    record_date DATE NOT NULL UNIQUE,
    weight_kg NUMERIC(8,1) NOT NULL CHECK (weight_kg > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 每日围度记录表：字段可独立记录，每个自然日最多一条稀疏记录
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
-- 插入 4 条包含精确宏观营养素的初始测试数据
-- Füge 4 anfängliche Testdaten mit genauen Makronährstoffen ein
INSERT INTO food_items (name_zh, name_en, protein_per_100g, fat_per_100g, carbs_per_100g) VALUES
('白米饭', 'White Rice', 2.60, 0.30, 25.90),
('苹果', 'Apple', 0.20, 0.20, 13.80),
('水煮鸡胸肉', 'Boiled Chicken Breast', 22.50, 3.20, 0.00),
('燕麦片', 'Oatmeal', 16.90, 6.90, 66.30);