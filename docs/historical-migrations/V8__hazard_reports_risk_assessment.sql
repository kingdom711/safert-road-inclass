-- 고용노동부 고시 제2023-19호(사업장 위험성평가에 관한 지침) 준수용 확장 컬럼
-- - 빈도(L) × 강도(S) = 위험성(R) 정량 평가
-- - 법적 근거(산안법/산안기준규칙) JSON
-- - 통제 위계(Hierarchy of Controls) 4단계 JSON
-- - KRAS 6대 유해위험요인 분류

ALTER TABLE hazard_reports
    ADD COLUMN IF NOT EXISTS ai_hazard_classification VARCHAR(50),
    ADD COLUMN IF NOT EXISTS ai_unsafe_condition      VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS ai_unsafe_act            VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS ai_possible_accident     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS ai_severity_score        INTEGER,
    ADD COLUMN IF NOT EXISTS ai_severity_rationale    VARCHAR(500),
    ADD COLUMN IF NOT EXISTS ai_likelihood_score      INTEGER,
    ADD COLUMN IF NOT EXISTS ai_likelihood_rationale  VARCHAR(500),
    ADD COLUMN IF NOT EXISTS ai_risk_score            INTEGER,
    ADD COLUMN IF NOT EXISTS ai_legal_basis_json      TEXT,
    ADD COLUMN IF NOT EXISTS ai_kosha_guide           VARCHAR(50),
    ADD COLUMN IF NOT EXISTS ai_control_measures_json TEXT,
    ADD COLUMN IF NOT EXISTS ai_responsible_role      VARCHAR(50),
    ADD COLUMN IF NOT EXISTS ai_due_days              INTEGER,
    ADD COLUMN IF NOT EXISTS ai_confidence            DOUBLE PRECISION;

CREATE INDEX IF NOT EXISTS idx_hr_risk_score ON hazard_reports(ai_risk_score);
CREATE INDEX IF NOT EXISTS idx_hr_hazard_classification ON hazard_reports(ai_hazard_classification);
