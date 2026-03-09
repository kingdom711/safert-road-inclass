-- =============================================
-- 교육 수료 기록 시스템 스키마
-- V4: Education Completions
-- =============================================

-- =============================================
-- 1. education_completions (교육 완료 마스터)
-- =============================================
CREATE TABLE IF NOT EXISTS education_completions (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- 교육 식별 정보
    education_id         VARCHAR(100) NOT NULL,               -- educationData.js의 콘텐츠 ID
    education_title      VARCHAR(255) NOT NULL,               -- 교육 제목
    education_type       VARCHAR(20)  NOT NULL DEFAULT 'GENERAL', -- GENERAL | SPECIALIZATION

    -- 시청 시간 기록
    started_at           TIMESTAMP    NOT NULL,               -- 교육 시작 시각 (클라이언트 제출)
    completed_at         TIMESTAMP    NOT NULL,               -- 교육 완료 시각 (서버 기록)
    video_watch_seconds  INTEGER      NOT NULL DEFAULT 0,     -- 실제 시청 시간(초)
    video_total_seconds  INTEGER      NOT NULL DEFAULT 0,     -- 영상 총 길이(초)
    video_watch_ratio    DECIMAL(5,4) NOT NULL DEFAULT 0,     -- 시청 비율 (ex: 0.9500)

    -- 퀴즈 결과
    quiz_score           INTEGER      NOT NULL DEFAULT 0,     -- 점수 (0~100)
    quiz_passed          BOOLEAN      NOT NULL DEFAULT FALSE,
    attempt_count        INTEGER      NOT NULL DEFAULT 1,     -- 총 시도 횟수

    -- 접속 환경 (위변조 검증용)
    ip_address           VARCHAR(45),                         -- IPv4/IPv6
    user_agent           TEXT,
    device_hash          VARCHAR(64),                         -- 디바이스 핑거프린트 해시

    -- 수료 증명
    cert_number          VARCHAR(100) UNIQUE,                 -- 수료증 번호 (UUID 기반)
    integrity_hash       VARCHAR(64),                         -- SHA-256 무결성 해시
    prev_hash            VARCHAR(64),                         -- 이전 레코드 해시 (체이닝)
    tsa_token            TEXT,                                -- 공인 타임스탬프 (Phase 2)

    created_at           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_edu_completion_user_id  ON education_completions(user_id);
CREATE INDEX IF NOT EXISTS idx_edu_completion_edu_id   ON education_completions(education_id);
CREATE INDEX IF NOT EXISTS idx_edu_completion_cert     ON education_completions(cert_number);
CREATE INDEX IF NOT EXISTS idx_edu_completion_user_edu ON education_completions(user_id, education_id);

-- Row Level Security 비활성화 (API 레이어에서 인증 처리)
ALTER TABLE education_completions DISABLE ROW LEVEL SECURITY;
