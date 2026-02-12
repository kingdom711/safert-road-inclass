-- =============================================
-- Safety Road - 전체 스키마 (Supabase SQL Editor에서 실행)
-- 실행 순서가 중요합니다! 위에서 아래로 순서대로 실행하세요.
-- =============================================

-- =============================================
-- 1. users (기본 테이블 - 다른 모든 테이블이 참조)
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 2. alerts (이미 존재하면 스킵됨)
-- =============================================
CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'INFO',
    priority INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_by_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_alerts_active ON alerts(active);
CREATE INDEX IF NOT EXISTS idx_alerts_type ON alerts(type);
CREATE INDEX IF NOT EXISTS idx_alerts_created_at ON alerts(created_at);

-- =============================================
-- 3. user_points (유저별 포인트 잔액, 1:1)
-- =============================================
CREATE TABLE IF NOT EXISTS user_points (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    balance INTEGER NOT NULL DEFAULT 0,
    total_earned INTEGER NOT NULL DEFAULT 0,
    total_spent INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 4. point_transactions (포인트 거래 내역)
-- =============================================
CREATE TABLE IF NOT EXISTS point_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount INTEGER NOT NULL,
    type VARCHAR(20) NOT NULL,
    reason VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    balance_after INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_point_txn_user_id ON point_transactions(user_id);

-- =============================================
-- 5. activity_logs (통합 활동 기록)
-- =============================================
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_activity_log_user_id ON activity_logs(user_id);

-- =============================================
-- 6. attendance_records (일별 출석 기록)
-- =============================================
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    check_in_date DATE NOT NULL,
    points_earned INTEGER NOT NULL DEFAULT 0,
    streak_at_time INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, check_in_date)
);

-- =============================================
-- 7. user_streaks (연속 출석 스트릭, 1:1)
-- =============================================
CREATE TABLE IF NOT EXISTS user_streaks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    current_streak INTEGER NOT NULL DEFAULT 0,
    longest_streak INTEGER NOT NULL DEFAULT 0,
    last_check_in_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 8. user_game_profiles (게임 프로필, 1:1)
-- =============================================
CREATE TABLE IF NOT EXISTS user_game_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    level INTEGER NOT NULL DEFAULT 1,
    exp INTEGER NOT NULL DEFAULT 0,
    exp_to_next INTEGER NOT NULL DEFAULT 100,
    game_role VARCHAR(50),
    active_specialization VARCHAR(50),
    total_quests_completed INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 9. user_specializations (전직/특수역할 해금 내역)
-- =============================================
CREATE TABLE IF NOT EXISTS user_specializations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    spec_id VARCHAR(50) NOT NULL,
    unlocked_at TIMESTAMP,
    education_progress TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, spec_id)
);

-- =============================================
-- 10. user_gold (골드 잔액, 1:1)
-- =============================================
CREATE TABLE IF NOT EXISTS user_gold (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    balance INTEGER NOT NULL DEFAULT 0,
    total_earned INTEGER NOT NULL DEFAULT 0,
    total_spent INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 11. gold_transactions (골드 거래 내역)
-- =============================================
CREATE TABLE IF NOT EXISTS gold_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount INTEGER NOT NULL,
    type VARCHAR(20) NOT NULL,
    reason VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    balance_after INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_gold_txn_user_id ON gold_transactions(user_id);

-- =============================================
-- 12. rewards (보상 아이템 - 관리자 등록)
-- =============================================
CREATE TABLE IF NOT EXISTS rewards (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    gold_price INTEGER NOT NULL,
    cash_value INTEGER NOT NULL,
    image_url VARCHAR(500),
    type VARCHAR(20) NOT NULL,
    total_quantity INTEGER NOT NULL,
    remaining_quantity INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 13. user_rewards (사용자 보상 교환 내역)
-- =============================================
CREATE TABLE IF NOT EXISTS user_rewards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reward_id BIGINT NOT NULL REFERENCES rewards(id) ON DELETE CASCADE,
    gold_paid INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    coupon_code VARCHAR(100),
    delivered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_rewards_user_id ON user_rewards(user_id);

-- =============================================
-- 14. work_types (작업 유형 마스터)
-- =============================================
CREATE TABLE IF NOT EXISTS work_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 15. checklist_templates (체크리스트 템플릿)
-- =============================================
CREATE TABLE IF NOT EXISTS checklist_templates (
    id BIGSERIAL PRIMARY KEY,
    work_type_id BIGINT NOT NULL REFERENCES work_types(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 16. template_items (템플릿 항목)
-- =============================================
CREATE TABLE IF NOT EXISTS template_items (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES checklist_templates(id) ON DELETE CASCADE,
    item_order INTEGER NOT NULL,
    content VARCHAR(500) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 17. checklists (체크리스트)
-- =============================================
CREATE TABLE IF NOT EXISTS checklists (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES checklist_templates(id),
    created_by BIGINT NOT NULL REFERENCES users(id),
    site_name VARCHAR(100) NOT NULL,
    work_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remarks VARCHAR(500),
    submitted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 18. checklist_items (체크리스트 항목)
-- =============================================
CREATE TABLE IF NOT EXISTS checklist_items (
    id BIGSERIAL PRIMARY KEY,
    checklist_id BIGINT NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    template_item_id BIGINT NOT NULL REFERENCES template_items(id),
    answer VARCHAR(10) NOT NULL,
    comment VARCHAR(500),
    risk_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 19. photos (사진)
-- =============================================
CREATE TABLE IF NOT EXISTS photos (
    id BIGSERIAL PRIMARY KEY,
    checklist_id BIGINT NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    original_name VARCHAR(255),
    stored_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    photo_type VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 20. risk_assessments (위험성 평가)
-- =============================================
CREATE TABLE IF NOT EXISTS risk_assessments (
    id BIGSERIAL PRIMARY KEY,
    checklist_item_id BIGINT NOT NULL UNIQUE REFERENCES checklist_items(id) ON DELETE CASCADE,
    assessed_by BIGINT NOT NULL REFERENCES users(id),
    frequency INTEGER NOT NULL,
    severity INTEGER NOT NULL,
    risk_score INTEGER NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    description VARCHAR(1000),
    assessed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 21. countermeasures (개선 대책)
-- =============================================
CREATE TABLE IF NOT EXISTS countermeasures (
    id BIGSERIAL PRIMARY KEY,
    risk_assessment_id BIGINT NOT NULL REFERENCES risk_assessments(id) ON DELETE CASCADE,
    created_by BIGINT NOT NULL REFERENCES users(id),
    content VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    due_date DATE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 22. action_records (조치 기록)
-- =============================================
CREATE TABLE IF NOT EXISTS action_records (
    id BIGSERIAL PRIMARY KEY,
    countermeasure_id BIGINT NOT NULL REFERENCES countermeasures(id) ON DELETE CASCADE,
    created_by BIGINT NOT NULL REFERENCES users(id),
    action_content VARCHAR(500),
    before_photo_path VARCHAR(500),
    after_photo_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 23. review_logs (검토 로그)
-- =============================================
CREATE TABLE IF NOT EXISTS review_logs (
    id BIGSERIAL PRIMARY KEY,
    checklist_id BIGINT NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES users(id),
    action VARCHAR(20) NOT NULL,
    reviewer_role VARCHAR(20) NOT NULL,
    comment VARCHAR(1000),
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    reviewed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- RLS 비활성화 (Spring Boot API 서버가 직접 접근)
-- =============================================
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE alerts DISABLE ROW LEVEL SECURITY;
ALTER TABLE user_points DISABLE ROW LEVEL SECURITY;
ALTER TABLE point_transactions DISABLE ROW LEVEL SECURITY;
ALTER TABLE activity_logs DISABLE ROW LEVEL SECURITY;
ALTER TABLE attendance_records DISABLE ROW LEVEL SECURITY;
ALTER TABLE user_streaks DISABLE ROW LEVEL SECURITY;
ALTER TABLE user_game_profiles DISABLE ROW LEVEL SECURITY;
ALTER TABLE user_specializations DISABLE ROW LEVEL SECURITY;
ALTER TABLE user_gold DISABLE ROW LEVEL SECURITY;
ALTER TABLE gold_transactions DISABLE ROW LEVEL SECURITY;
ALTER TABLE rewards DISABLE ROW LEVEL SECURITY;
ALTER TABLE user_rewards DISABLE ROW LEVEL SECURITY;
ALTER TABLE work_types DISABLE ROW LEVEL SECURITY;
ALTER TABLE checklist_templates DISABLE ROW LEVEL SECURITY;
ALTER TABLE template_items DISABLE ROW LEVEL SECURITY;
ALTER TABLE checklists DISABLE ROW LEVEL SECURITY;
ALTER TABLE checklist_items DISABLE ROW LEVEL SECURITY;
ALTER TABLE photos DISABLE ROW LEVEL SECURITY;
ALTER TABLE risk_assessments DISABLE ROW LEVEL SECURITY;
ALTER TABLE countermeasures DISABLE ROW LEVEL SECURITY;
ALTER TABLE action_records DISABLE ROW LEVEL SECURITY;
ALTER TABLE review_logs DISABLE ROW LEVEL SECURITY;

-- =============================================
-- 완료! 총 23개 테이블 생성
-- =============================================
