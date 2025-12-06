# REQ-FUNC-05: 안전관리자 위험현황 조회 및 조치 기록

## 1. 개요
- **목적**: 위험 요소가 있는 체크리스트를 필터링하고 조치 이력을 남김.
- **SRS 섹션**: 3.5 F-05
- **구성 요소**:
    - BE: 필터링 검색 API, 조치(Action) 엔티티 CRUD
    - FE: 대시보드 필터 UI, 조치 기록 폼

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "REQ-FUNC-05-RISK-ACTION"
title: "위험현황 대시보드 및 조치 이력 관리 기능 구현"
summary: >
  '아니요' 항목이 있거나 AI가 위험으로 판단한 건을 우선 노출하는 검색 API와,
  안전관리자가 이에 대한 조치(ActionRecord)를 등록/수정하는 기능을 구현한다.
type: "functional"
epic: "EPIC_2_RISK_MGMT"
req_ids: ["F-05"]
component: ["backend", "frontend", "database"]

context:
  apis:
    - "GET /api/checklists/risks"
    - "POST /api/checklists/{id}/actions"
  entities: ["ActionRecord", "ActionPhoto"]

inputs:
  description: "검색 조건(기간, 현장, 위험도), 조치 내용(텍스트, 사진)"

outputs:
  description: "위험 리스트, 생성된 조치 ID"

steps_hint:
  - "DB: ActionRecord 테이블 설계 (1:N 관계)"
  - "BE: 복합 조건 검색 API 구현 (Risk Flag=true OR AI Risk=DANGER)"
  - "BE: 조치 등록 시 ChecklistInstance의 action_status 업데이트 (None -> InProgress -> Completed)"
  - "FE: 대시보드형 리스트 UI 구현"

preconditions:
  - "REQ-FUNC-03-WRITE"

postconditions:
  - "위험 항목만 필터링되어 조회된다."
  - "조치 기록이 저장되고, 해당 체크리스트의 조치 상태가 갱신된다."

dependencies: ["REQ-FUNC-03-WRITE"]
parallelizable: true
estimated_effort: "M"
priority: "Must"
agent_profile: ["backend", "frontend"]
```

