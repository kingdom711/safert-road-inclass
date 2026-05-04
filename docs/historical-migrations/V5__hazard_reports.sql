-- 위험 발견-조치 완료 1사이클 테이블
CREATE TABLE IF NOT EXISTS hazard_reports (
    id                    BIGSERIAL PRIMARY KEY,
    reporter_id           BIGINT NOT NULL REFERENCES users(id),

    -- Step 1: 위험 발견
    hazard_photo_path     VARCHAR(500) NOT NULL,
    hazard_description    VARCHAR(2000),
    location_description  VARCHAR(500),
    reported_at           TIMESTAMP NOT NULL,

    -- Step 2: AI 분석 결과
    ai_risk_level         VARCHAR(20),
    ai_risk_factor        VARCHAR(1000),
    ai_remediation_steps  TEXT,
    ai_reference_code     VARCHAR(50),
    ai_analyzed_at        TIMESTAMP,
    ai_token_usage        INTEGER,

    -- Step 3: 조치 완료
    completion_photo_path VARCHAR(500),
    completion_note       VARCHAR(2000),
    completed_at          TIMESTAMP,

    -- 상태
    status                VARCHAR(30) NOT NULL DEFAULT 'HAZARD_REPORTED',

    -- 보상 추적
    tier1_points_awarded  INTEGER DEFAULT 0,
    tier1_awarded_at      TIMESTAMP,
    tier2_points_awarded  INTEGER DEFAULT 0,
    tier2_awarded_at      TIMESTAMP,

    -- 오프라인 동기화
    client_temp_id        VARCHAR(100) UNIQUE,
    synced_from_offline   BOOLEAN DEFAULT FALSE,

    -- 감사 필드
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 사이클당 다중 사진 지원
CREATE TABLE IF NOT EXISTS hazard_report_photos (
    id                    BIGSERIAL PRIMARY KEY,
    hazard_report_id      BIGINT NOT NULL REFERENCES hazard_reports(id),
    photo_stage           VARCHAR(20) NOT NULL,
    stored_path           VARCHAR(500) NOT NULL,
    original_name         VARCHAR(255),
    content_type          VARCHAR(100),
    file_size             BIGINT,
    display_order         INTEGER DEFAULT 0,
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_hr_reporter_id ON hazard_reports(reporter_id);
CREATE INDEX IF NOT EXISTS idx_hr_status ON hazard_reports(status);
CREATE INDEX IF NOT EXISTS idx_hr_reported_at ON hazard_reports(reported_at);
CREATE INDEX IF NOT EXISTS idx_hr_client_temp_id ON hazard_reports(client_temp_id);
CREATE INDEX IF NOT EXISTS idx_hrp_report_id ON hazard_report_photos(hazard_report_id);
