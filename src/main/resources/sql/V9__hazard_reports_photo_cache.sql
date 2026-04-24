-- 사진 해시 기반 AI 분석 캐시
-- 동일 사진이 CACHE_TTL_DAYS(7일) 내에 재분석 요청되면 기존 결과를 복사해 Gemini API 호출을 절감한다.

ALTER TABLE hazard_reports
    ADD COLUMN IF NOT EXISTS photo_sha256       VARCHAR(64),
    ADD COLUMN IF NOT EXISTS ai_cache_source_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_hr_photo_sha256 ON hazard_reports(photo_sha256);
