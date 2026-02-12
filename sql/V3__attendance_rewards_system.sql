-- =============================================
-- 출석 보상 시스템 스키마
-- V3: Attendance Rewards System
-- =============================================

-- =============================================
-- 1. item_boxes (아이템 상자 마스터)
-- =============================================
CREATE TABLE IF NOT EXISTS item_boxes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    box_type VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 2. box_rewards (상자 보상 정의)
-- =============================================
CREATE TABLE IF NOT EXISTS box_rewards (
    id BIGSERIAL PRIMARY KEY,
    box_id BIGINT NOT NULL REFERENCES item_boxes(id) ON DELETE CASCADE,
    item_id VARCHAR(50) NOT NULL,
    weight INTEGER NOT NULL DEFAULT 100,
    min_quantity INTEGER DEFAULT 1,
    max_quantity INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_box_rewards_box_id ON box_rewards(box_id);

-- =============================================
-- 3. attendance_rewards (출석 보상 마스터)
-- =============================================
CREATE TABLE IF NOT EXISTS attendance_rewards (
    id BIGSERIAL PRIMARY KEY,
    day_number INTEGER NOT NULL,
    reward_type VARCHAR(20) NOT NULL,
    points_amount INTEGER DEFAULT 0,
    box_id BIGINT REFERENCES item_boxes(id),
    is_perfect_attendance BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(day_number, reward_type)
);

-- =============================================
-- 4. user_attendance_rewards (사용자 출석 보상 수령)
-- =============================================
CREATE TABLE IF NOT EXISTS user_attendance_rewards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    attendance_reward_id BIGINT NOT NULL REFERENCES attendance_rewards(id),
    attendance_record_id BIGINT REFERENCES attendance_records(id),
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    claimed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, attendance_reward_id, year, month)
);
CREATE INDEX IF NOT EXISTS idx_user_att_rewards_user_status ON user_attendance_rewards(user_id, status);
CREATE INDEX IF NOT EXISTS idx_user_att_rewards_year_month ON user_attendance_rewards(user_id, year, month);

-- =============================================
-- 5. user_inventory_items (사용자 인벤토리)
-- =============================================
CREATE TABLE IF NOT EXISTS user_inventory_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_id VARCHAR(50) NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    source VARCHAR(50),
    source_id BIGINT,
    obtained_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_inv_user_type ON user_inventory_items(user_id, item_type);
CREATE INDEX IF NOT EXISTS idx_user_inv_user_item ON user_inventory_items(user_id, item_id);

-- Row Level Security 비활성화 (API 접근 허용)
ALTER TABLE item_boxes DISABLE ROW LEVEL SECURITY;
ALTER TABLE box_rewards DISABLE ROW LEVEL SECURITY;
ALTER TABLE attendance_rewards DISABLE ROW LEVEL SECURITY;
ALTER TABLE user_attendance_rewards DISABLE ROW LEVEL SECURITY;
ALTER TABLE user_inventory_items DISABLE ROW LEVEL SECURITY;
