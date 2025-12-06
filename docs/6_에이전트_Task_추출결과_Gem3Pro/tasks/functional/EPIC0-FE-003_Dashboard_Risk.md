# EPIC0-FE-003: 위험현황 대시보드 및 조치 기록 PoC

## 1. 개요
- **목적**: 안전관리자가 위험 현황을 한눈에 보고, 조치 사항을 기록하는 UI를 구현한다.
- **관련 REQ**: F-05, F-06
- **범위**:
    - 위험도별 필터링 리스트 (카드 뷰/리스트 뷰)
    - 상세 모달/페이지 (AI 분석 결과 표시 포함)
    - 조치 내용 입력 폼

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "EPIC0-FE-003-DASHBOARD"
title: "위험현황 모니터링 및 조치 기록 UI PoC 구현"
summary: >
  '아니요' 응답이나 'AI 위험' 판정 건을 필터링하여 보여주는 대시보드 리스트와,
  선택 시 상세 정보를 보고 조치(Action)를 입력하는 UI를 구현한다.
type: "functional"
epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["F-05", "F-06"]
component: ["frontend"]

context:
  mock_risks:
    - type: "LADDER"
      risk_level: "HIGH"
      ai_tags: ["helmet_missing", "ladder_unsecured"]
      status: "SUBMITTED"

inputs:
  description: "Mock 위험 체크리스트 목록"

outputs:
  description: "조치 기록 입력 UI 동작"
  success:
    - "리스트에서 위험 건 클릭 시 상세 뷰 진입"
    - "조치 사항 입력 후 '조치 완료' 클릭 시 상태 변경(UI 반영)"

steps_hint:
  - "RiskList 컴포넌트 (위험도 배지, AI 태그 표시)"
  - "FilterBar 컴포넌트 (상태별/유형별 필터)"
  - "ActionInput 컴포넌트 (텍스트 + 사진 첨부 UI)"
  - "상세 모달 또는 페이지 라우팅 연결"

preconditions:
  - "EPIC0-FE-001-LAYOUT 완료"

postconditions:
  - "위험 항목을 식별하고 조치 내용을 입력하는 UX가 검증된다."

dependencies: ["EPIC0-FE-001-LAYOUT"]
parallelizable: true
estimated_effort: "M"
priority: "Should"
agent_profile: ["frontend"]
```

