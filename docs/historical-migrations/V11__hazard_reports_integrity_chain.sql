-- Hazard Cycle 무결성 해시체인
-- - cert_number: UUID (QR 검증 URL 구성용)
-- - prev_hash: 직전 봉인 사이클의 integrity_hash (해시체인 연결)
-- - integrity_hash: SHA-256 — 조작 감지용

ALTER TABLE hazard_reports
    ADD COLUMN IF NOT EXISTS cert_number    VARCHAR(36),
    ADD COLUMN IF NOT EXISTS prev_hash      VARCHAR(64),
    ADD COLUMN IF NOT EXISTS integrity_hash VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uk_hr_cert_number ON hazard_reports(cert_number);
CREATE INDEX IF NOT EXISTS idx_hr_integrity_hash ON hazard_reports(integrity_hash);
