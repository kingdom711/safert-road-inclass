-- =============================================
-- 교육 수료 레코드 불변성 강제 (위변조 방지)
-- V7: Immutability Triggers for education_completions
--
-- 목적: 고용노동부 사후 점검 시 "수료 레코드가 생성 후 수정/삭제되지 않았다"는
-- 기술적 증거를 제공. INSERT 이후에는 해시 체인(integrity_hash, prev_hash,
-- cert_number)만 1회 채워지는 UPDATE가 허용되고 그 외 UPDATE/DELETE는 차단.
-- =============================================

-- ─── 1. UPDATE 차단 트리거 ──────────────────────────────────────
-- 예외: 최초 저장 직후 서비스 레이어에서 해시 체인 정보를 채우는 UPDATE 1회만 허용.
--       (integrity_hash IS NULL → NOT NULL 전환, prev_hash/cert_number 동일)
CREATE OR REPLACE FUNCTION trg_education_completions_prevent_update()
RETURNS TRIGGER AS $$
BEGIN
    -- 식별 정보는 어떤 경우에도 변경 불가
    IF NEW.id                <> OLD.id
    OR NEW.user_id           <> OLD.user_id
    OR NEW.education_id      <> OLD.education_id
    OR NEW.started_at        <> OLD.started_at
    OR NEW.completed_at      <> OLD.completed_at
    OR NEW.video_watch_seconds <> OLD.video_watch_seconds
    OR NEW.video_total_seconds <> OLD.video_total_seconds
    OR NEW.video_watch_ratio <> OLD.video_watch_ratio
    OR NEW.quiz_score        <> OLD.quiz_score
    OR NEW.quiz_passed       <> OLD.quiz_passed
    OR NEW.attempt_count     <> OLD.attempt_count THEN
        RAISE EXCEPTION 'education_completions core columns are immutable (id=%)', OLD.id
            USING ERRCODE = '45000';
    END IF;

    -- 해시 체인은 최초 1회만 채울 수 있음 (NULL → 값). 이미 채워진 값을 바꿀 수 없음.
    IF OLD.integrity_hash IS NOT NULL AND NEW.integrity_hash IS DISTINCT FROM OLD.integrity_hash THEN
        RAISE EXCEPTION 'integrity_hash is write-once (id=%)', OLD.id
            USING ERRCODE = '45000';
    END IF;
    IF OLD.cert_number IS NOT NULL AND NEW.cert_number IS DISTINCT FROM OLD.cert_number THEN
        RAISE EXCEPTION 'cert_number is write-once (id=%)', OLD.id
            USING ERRCODE = '45000';
    END IF;
    IF OLD.prev_hash IS NOT NULL AND NEW.prev_hash IS DISTINCT FROM OLD.prev_hash THEN
        RAISE EXCEPTION 'prev_hash is write-once (id=%)', OLD.id
            USING ERRCODE = '45000';
    END IF;

    -- updated_at 자동 갱신
    NEW.updated_at := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS prevent_education_completion_update ON education_completions;
CREATE TRIGGER prevent_education_completion_update
    BEFORE UPDATE ON education_completions
    FOR EACH ROW
    EXECUTE FUNCTION trg_education_completions_prevent_update();


-- ─── 2. DELETE 차단 트리거 ──────────────────────────────────────
CREATE OR REPLACE FUNCTION trg_education_completions_prevent_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'education_completions records cannot be deleted (id=%)', OLD.id
        USING ERRCODE = '45000';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS prevent_education_completion_delete ON education_completions;
CREATE TRIGGER prevent_education_completion_delete
    BEFORE DELETE ON education_completions
    FOR EACH ROW
    EXECUTE FUNCTION trg_education_completions_prevent_delete();


-- ─── 3. 시청 로그도 삭제 차단 (수정은 어차피 일어나지 않음) ────
CREATE OR REPLACE FUNCTION trg_watch_logs_prevent_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'education_watch_logs records cannot be deleted (id=%)', OLD.id
        USING ERRCODE = '45000';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS prevent_watch_log_delete ON education_watch_logs;
CREATE TRIGGER prevent_watch_log_delete
    BEFORE DELETE ON education_watch_logs
    FOR EACH ROW
    EXECUTE FUNCTION trg_watch_logs_prevent_delete();


-- ─── 4. 퀴즈 답안 로그도 삭제 차단 ──────────────────────────────
CREATE OR REPLACE FUNCTION trg_quiz_attempt_logs_prevent_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'quiz_attempt_logs records cannot be deleted (id=%)', OLD.id
        USING ERRCODE = '45000';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS prevent_quiz_log_delete ON quiz_attempt_logs;
CREATE TRIGGER prevent_quiz_log_delete
    BEFORE DELETE ON quiz_attempt_logs
    FOR EACH ROW
    EXECUTE FUNCTION trg_quiz_attempt_logs_prevent_delete();
