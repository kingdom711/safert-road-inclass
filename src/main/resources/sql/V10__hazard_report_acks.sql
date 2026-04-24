-- 동료 근로자 위험요인 확인 테이블
-- 산업안전보건법 제36조 2항 "근로자 참여" 증빙용.

CREATE TABLE IF NOT EXISTS hazard_report_acks (
    id               BIGSERIAL PRIMARY KEY,
    hazard_report_id BIGINT NOT NULL REFERENCES hazard_reports(id),
    acker_id         BIGINT NOT NULL REFERENCES users(id),
    comment          VARCHAR(500),
    acked_at         TIMESTAMP NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_hazard_ack_report_user UNIQUE (hazard_report_id, acker_id)
);

CREATE INDEX IF NOT EXISTS idx_hra_report_id ON hazard_report_acks(hazard_report_id);
CREATE INDEX IF NOT EXISTS idx_hra_acker_id ON hazard_report_acks(acker_id);
