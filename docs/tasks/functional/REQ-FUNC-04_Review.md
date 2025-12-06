# REQ-FUNC-04: 감독자 검토 및 승인/반려

## 1. 개요
- **목적**: 제출된 체크리스트를 감독자가 확인하고 상태를 변경함.
- **SRS 섹션**: 3.4 F-04
- **구성 요소**:
    - BE: 상태 변경 API, 반려 사유 저장
    - FE: 검토 대기 목록, 상세 뷰, 승인/반려 모달

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "REQ-FUNC-04-REVIEW"
title: "체크리스트 검토 프로세스 및 승인/반려 API 구현"
summary: >
  관리감독자가 제출된 체크리스트를 조회하고, 승인(APPROVED) 또는 반려(REJECTED) 처리하는 로직을 구현한다.
  반려 시 사유를 필수 입력받아 저장한다.
type: "functional"
epic: "EPIC_1_CHECKLIST"
req_ids: ["F-04"]
component: ["backend", "frontend"]

context:
  apis:
    - "GET /api/checklists?status=SUBMITTED"
    - "POST /api/checklists/{id}/approve"
    - "POST /api/checklists/{id}/reject"
  entities: ["ChecklistInstance"]

inputs:
  description: "체크리스트 ID, 승인여부, 반려사유(반려 시)"

outputs:
  description: "변경된 상태 결과"

steps_hint:
  - "BE: 검토 대기 목록 조회 쿼리 (QueryDSL 등 활용)"
  - "BE: 상태 전이(State Transition) 로직 검증 (SUBMITTED 상태에서만 가능)"
  - "FE: 검토 리스트 UI 및 상세 화면 구현"
  - "FE: 승인/반려 액션 버튼 및 사유 입력 모달 연동"

preconditions:
  - "REQ-FUNC-03-WRITE 완료 (데이터가 있어야 함)"

postconditions:
  - "승인 시 상태가 APPROVED_SUPERVISOR로 변경된다."
  - "반려 시 상태가 REJECTED_SUPERVISOR로 변경되고 사유가 저장된다."

dependencies: ["REQ-FUNC-03-WRITE"]
parallelizable: true
estimated_effort: "M"
priority: "Must"
agent_profile: ["backend", "frontend"]
```

