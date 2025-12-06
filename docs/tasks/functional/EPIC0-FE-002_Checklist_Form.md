# EPIC0-FE-002: 체크리스트 작성 및 제출 폼 PoC

## 1. 개요
- **목적**: 기술인의 핵심 과업인 '작업유형 선택 -> 체크리스트 작성 -> 사진 첨부 -> 제출' 흐름을 UI로 구현한다.
- **관련 REQ**: F-02, F-03
- **범위**:
    - 작업유형 선택 화면
    - 동적 체크리스트 폼 (네/아니요)
    - 사진 업로드 UI (미리보기 포함, 실제 전송 X)

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "EPIC0-FE-002-FORM"
title: "체크리스트 작성 및 제출 폼 PoC 구현"
summary: >
  작업유형(사다리/고소/밀폐)을 선택하고, 해당 유형의 체크 항목을 렌더링하며,
  응답(Yes/No) 및 사진을 첨부하여 제출하는 전체 UI 흐름을 구현한다.
type: "functional"
epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["F-02", "F-03"]
component: ["frontend"]

context:
  work_types: ["LADDER", "AERIAL", "CONFINED"]
  mock_data_ref: "checklists_template_mock.json"

inputs:
  description: "사용자 입력 및 Mock 템플릿 데이터"
  preloaded_state:
    - "작업유형별 체크리스트 항목 Mock Data 준비"

outputs:
  description: "작성 완료된 폼 데이터(Console Log 확인)"
  success:
    - "모든 필수 항목 체크 시 제출 버튼 활성화"
    - "제출 클릭 시 입력된 데이터가 JSON 형태로 콘솔에 출력"

steps_hint:
  - "WorkTypeSelector 컴포넌트 구현"
  - "ChecklistItem 컴포넌트 구현 (Radio Group: 네/아니요)"
  - "ImageUploader UI 구현 (Input type=file, Preview)"
  - "전체 Form State 관리 (React Hook Form 등 활용 권장)"

preconditions:
  - "EPIC0-FE-001-LAYOUT 완료 (해당 레이아웃 안에서 동작)"

postconditions:
  - "사용자가 폼을 채우고 제출 버튼을 누르면 입력 데이터가 수집된다."

dependencies: ["EPIC0-FE-001-LAYOUT"]
parallelizable: true
estimated_effort: "M"
priority: "Must"
agent_profile: ["frontend"]
```

