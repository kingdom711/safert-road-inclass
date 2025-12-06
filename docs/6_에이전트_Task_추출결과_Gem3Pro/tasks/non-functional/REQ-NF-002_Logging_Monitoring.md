# REQ-NF-002: 로깅 및 모니터링 기초

## 1. 개요
- **목적**: 운영 중 발생하는 이슈 추적 및 감사(Audit)를 위한 로깅.
- **SRS 섹션**: 7.3
- **구성 요소**: Logback/SLF4J, AOP 기반 감사 로그, AWS CloudWatch(Optional)

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "REQ-NF-002-LOGGING"
title: "구조화된 로깅 및 감사(Audit) 로그 구현"
summary: >
  주요 비즈니스 이벤트(로그인, 제출, 승인/반려, 조치)에 대해 
  AOP를 활용하여 일관된 포맷의 감사 로그를 남기고, 에러 로그를 파일/콘솔에 구조화하여 출력한다.
type: "non_functional"
epic: "EPIC_INFRA"
req_ids: ["NF-03"]
component: ["backend"]
category: "observability"
labels: ["logging:audit", "logging:error"]

context:
  related_req_func: ["F-01", "F-03", "F-04", "F-05"]

requirements:
  description: "누가, 언제, 무엇을 했는지 추적 가능해야 함"
  kpis:
    - "주요 이벤트 로그 누락 없음"

steps_hint:
  - "BE: Logback 설정 (Console + File Appender, JSON 포맷 고려)"
  - "BE: @AuditLog 어노테이션 및 AOP Aspect 구현"
  - "BE: AuditLog 엔티티 및 저장 로직 구현 (DB 저장 방식)"
  - "BE: GlobalExceptionHandler에서 예외 로그 남기기"

preconditions:
  - "REQ-FUNC 구현 진행 중"

postconditions:
  - "로그인, 제출 등 행위 발생 시 AuditLog 테이블에 레코드가 생성된다."

dependencies: ["REQ-FUNC-01-AUTH"]
parallelizable: true
estimated_effort: "M"
priority: "Should"
agent_profile: ["backend"]
```

