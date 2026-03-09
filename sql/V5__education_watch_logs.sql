-- =============================================
-- 영상 시청 체크포인트 로그
-- V5: Education Watch Logs
-- =============================================

-- =============================================
-- 1. education_watch_logs (시청 체크포인트 기록)
-- 30초 간격으로 클라이언트 → 서버 전송
-- 서버 타임스탬프(server_time)만 신뢰 (클라이언트 시각 불신뢰)
-- =============================================
CREATE TABLE IF NOT EXISTS education_watch_logs (
    id              BIGSERIAL  PRIMARY KEY,
    completion_id   BIGINT     REFERENCES education_completions(id) ON DELETE SET NULL,
    user_id         BIGINT     NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    education_id    VARCHAR(100) NOT NULL,

    -- 시청 상태 (클라이언트 보고값)
    checkpoint_sec  INTEGER    NOT NULL,               -- 현재 재생 위치(초)
    total_sec       INTEGER    NOT NULL,               -- 영상 전체 길이(초)
    is_playing      BOOLEAN    NOT NULL DEFAULT TRUE,  -- 재생 중 여부
    tab_visible     BOOLEAN    NOT NULL DEFAULT TRUE,  -- 탭 포커스(활성) 여부

    -- 서버 기록 시각 (위변조 방지 핵심)
    server_time     TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_watch_log_user_edu ON education_watch_logs(user_id, education_id);
CREATE INDEX IF NOT EXISTS idx_watch_log_completion ON education_watch_logs(completion_id);

ALTER TABLE education_watch_logs DISABLE ROW LEVEL SECURITY;
