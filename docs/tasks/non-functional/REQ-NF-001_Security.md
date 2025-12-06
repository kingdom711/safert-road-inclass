# REQ-NF-001: 보안 및 인증 아키텍처

## 1. 개요
- **목적**: 안전한 서비스를 위한 기본 보안 요건 충족.
- **SRS 섹션**: 7.2
- **구성 요소**: Spring Security, JWT, Password Encoder, S3 Presigned URL(Optional)

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "REQ-NF-001-SECURITY"
title: "보안 아키텍처 및 데이터 보호 적용"
summary: >
  비밀번호 해시 저장(BCrypt), API 엔드포인트 보안 설정, CORS/CSRF 정책 수립,
  그리고 파일 업로드 시 확장자 검증 등의 보안 조치를 적용한다.
type: "non_functional"
epic: "EPIC_INFRA"
req_ids: ["NF-02"]
component: ["backend", "infra"]
category: "security"
labels: ["security:auth", "security:data"]

context:
  related_req_func: ["F-01", "F-03"]

requirements:
  description: "OWASP Top 10 대응 및 개인정보/민감정보 보호"
  kpis:
    - "비밀번호 평문 저장 0건"
    - "허용되지 않은 파일 확장자 업로드 차단 100%"

steps_hint:
  - "BE: SecurityConfig - URL별 접근 권한 정교화"
  - "BE: PasswordEncoder Bean 등록 및 적용"
  - "BE: 파일 업로드 시 Mime Type 및 확장자 이중 체크 로직 추가"
  - "Infra: DB 접속 정보 등 민감 환경변수 암호화 관리 (또는 Secret Manager)"

preconditions:
  - "기본 백엔드 프로젝트 구성 완료"

postconditions:
  - "인증되지 않은 요청은 차단된다."
  - "악성 파일 업로드가 거부된다."

dependencies: ["REQ-FUNC-01-AUTH"]
parallelizable: true
estimated_effort: "M"
priority: "Must"
agent_profile: ["backend", "security"]
```

