-- =============================================
-- 퀴즈 개별 답안 이력
-- V6: Quiz Attempt Logs
-- =============================================

-- =============================================
-- 1. quiz_attempt_logs (퀴즈 답안 상세 기록)
-- 문항별 선택지, 정오, 소요시간을 저장하여
-- "실제로 퀴즈를 직접 풀었음"을 증명
-- =============================================
CREATE TABLE IF NOT EXISTS quiz_attempt_logs (
    id              BIGSERIAL    PRIMARY KEY,
    completion_id   BIGINT       REFERENCES education_completions(id) ON DELETE SET NULL,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    education_id    VARCHAR(100) NOT NULL,
    attempt_number  INTEGER      NOT NULL DEFAULT 1,  -- 몇 번째 시도인지

    -- 문항별 답안
    question_id     VARCHAR(100) NOT NULL,            -- 문항 ID
    selected_option INTEGER      NOT NULL,            -- 선택한 보기 번호 (0-based)
    is_correct      BOOLEAN      NOT NULL,
    time_spent_sec  INTEGER,                          -- 해당 문항 소요 시간(초)

    submitted_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_quiz_log_user_edu ON quiz_attempt_logs(user_id, education_id);
CREATE INDEX IF NOT EXISTS idx_quiz_log_completion ON quiz_attempt_logs(completion_id);

ALTER TABLE quiz_attempt_logs DISABLE ROW LEVEL SECURITY;
