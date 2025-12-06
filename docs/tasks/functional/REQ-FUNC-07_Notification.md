# REQ-FUNC-07: 알림 및 내 할 일

## 1. 개요
- **목적**: 사용자 행동 유도를 위한 심플한 To-Do 리스트 제공.
- **SRS 섹션**: 3.7 F-07
- **구성 요소**:
    - BE: 내 할 일 조회 API (각 역할별 조건 조회)
    - FE: 메인 대시보드 위젯

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "REQ-FUNC-07-NOTI-TODO"
title: "역할별 '내 할 일(My Todo)' 조회 기능 구현"
summary: >
  별도의 알림 테이블 없이, 상태 기반 쿼리를 통해 
  기술인(반려 확인), 감독자(미승인 건), 안전관리자(미조치 고위험 건)의
  '해야 할 일' 목록을 조회하는 API와 UI를 구현한다.
type: "functional"
epic: "EPIC_1_CHECKLIST"
req_ids: ["F-07"]
component: ["backend", "frontend"]

context:
  apis:
    - "GET /api/users/me/todos"

inputs:
  description: "현재 로그인한 사용자 컨텍스트"

outputs:
  description: "Todo 아이템 리스트 (Type, Message, Link)"

steps_hint:
  - "BE: TodoService 구현 - 역할에 따라 다른 Repository Query 실행"
    - "Worker: status=REJECTED"
    - "Supervisor: status=SUBMITTED"
    - "SafetyManager: risk=true AND action_status!=COMPLETED"
  - "FE: 메인 화면 상단에 Todo List 위젯 배치"

preconditions:
  - "REQ-FUNC-01 ~ 05 전체 프로세스 로직이 존재해야 쿼리 가능"

postconditions:
  - "상태 변경 시 즉시 내 할 일 목록에 반영된다(조회 시점 기준)."

dependencies: ["REQ-FUNC-04-REVIEW", "REQ-FUNC-05-RISK-ACTION"]
parallelizable: true
estimated_effort: "S"
priority: "High"
agent_profile: ["backend", "frontend"]
```

