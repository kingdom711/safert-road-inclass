# REQ-FUNC-02: 작업유형 선택 및 템플릿 로딩

## 1. 개요
- **목적**: DB에 저장된 체크리스트 템플릿(JSON 구조 등)을 조회하여 클라이언트에 제공.
- **SRS 섹션**: 3.2 F-02
- **구성 요소**:
    - BE: 템플릿 조회 API, 템플릿 엔티티/데이터
    - FE: 작업유형 선택 UI, 동적 폼 렌더러

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "REQ-FUNC-02-TEMPLATE"
title: "체크리스트 템플릿 관리 및 조회 API 구현"
summary: >
  사다리, 고소작업대, 밀폐공간 등 작업유형별 체크리스트 항목을 DB에 구조화하여 저장하고,
  클라이언트 요청 시 해당 버전의 템플릿을 제공하는 기능을 구현한다.
type: "functional"
epic: "EPIC_1_CHECKLIST"
req_ids: ["F-02"]
component: ["backend", "database"]

context:
  apis:
    - "GET /api/templates?work_type={type}"
  entities: ["ChecklistTemplate", "ChecklistItemTemplate"]

inputs:
  description: "작업 유형 (LADDER, AERIAL, CONFINED)"

outputs:
  description: "체크리스트 항목 리스트 (JSON)"

steps_hint:
  - "DB: Template 및 ItemTemplate 테이블 설계 (1:N 관계)"
  - "DB: 초기 법정 자율점검표 데이터(사다리/고소/밀폐) 시딩"
  - "BE: TemplateController/Service 구현"
  - "BE: 템플릿 버전 관리 로직 (단순 버전 필드)"
  - "FE: API 연동 및 Redux/Context에 템플릿 데이터 캐싱 처리"

preconditions:
  - "REQ-FUNC-01-AUTH (API 접근 권한)"

postconditions:
  - "작업유형 요청 시 올바른 항목 리스트가 반환된다."

dependencies: ["REQ-FUNC-01-AUTH"]
parallelizable: true
estimated_effort: "S"
priority: "Must"
agent_profile: ["backend"]
```

