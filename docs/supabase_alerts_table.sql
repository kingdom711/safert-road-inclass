-- Supabase에서 alerts 테이블 생성 SQL
-- Supabase SQL Editor에서 실행하세요

-- alerts 테이블 생성
CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'INFO',
    priority INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_by_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- type 컬럼 체크 제약조건 (INFO, WARNING, DANGER, SUCCESS만 허용)
ALTER TABLE alerts 
ADD CONSTRAINT chk_alert_type 
CHECK (type IN ('INFO', 'WARNING', 'DANGER', 'SUCCESS'));

-- 인덱스 생성 (성능 최적화)
CREATE INDEX IF NOT EXISTS idx_alerts_active ON alerts(active);
CREATE INDEX IF NOT EXISTS idx_alerts_type ON alerts(type);
CREATE INDEX IF NOT EXISTS idx_alerts_created_at ON alerts(created_at);

-- Row Level Security (RLS) 비활성화 (API 접근 허용)
ALTER TABLE alerts DISABLE ROW LEVEL SECURITY;

-- 테스트 데이터 삽입 (선택사항)
-- INSERT INTO alerts (title, message, type, priority, active)
-- VALUES ('테스트 알림', '이것은 테스트 알림입니다.', 'INFO', 1, true);
