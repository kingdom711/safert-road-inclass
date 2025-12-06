# EPIC0-FE-001: 메인 레이아웃 및 네비게이션 PoC

## 1. 개요
- **목적**: 기술인, 관리감독자, 안전관리자가 접속했을 때 보여질 메인 레이아웃(GNB, Sidebar, Mobile Nav)과 기본 라우팅 구조를 잡는다.
- **관련 REQ**: F-01(권한), F-07(알림 접근)
- **범위**:
    - 반응형 레이아웃 (PC/Mobile)
    - 역할별(Role-based) 메뉴 노출 분기 처리
    - 로그인/로그아웃 더미 처리

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "EPIC0-FE-001-LAYOUT"
title: "전체 레이아웃 및 역할별 네비게이션 PoC 구현"
summary: >
  React(Vite) + TailwindCSS 기반의 공통 레이아웃을 구현한다.
  사용자 역할(기술인/감독자/안전관리자)에 따라 메뉴가 다르게 보이는지 검증하는 Mock Auth Context를 포함한다.
type: "functional"
epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["F-01", "F-07"]
component: ["frontend"]

context:
  roles: ["WORKER", "SUPERVISOR", "SAFETY_MANAGER"]
  menus:
    - label: "홈(내 할 일)"
      roles: ["ALL"]
    - label: "체크리스트 작성"
      roles: ["WORKER"]
    - label: "검토 대기함"
      roles: ["SUPERVISOR"]
    - label: "위험 현황"
      roles: ["SAFETY_MANAGER"]

inputs:
  description: "사용자 역할 정보 (Mock)"
  preloaded_state:
    - "localStorage 또는 Context에 현재 로그인된 사용자 Role이 저장되어 있다고 가정"

outputs:
  description: "반응형 레이아웃 및 라우팅 작동"
  success:
    - "모바일에서는 하단 탭바 또는 햄버거 메뉴 작동"
    - "PC에서는 사이드바 또는 상단 GNB 작동"
    - "Role 변경 시 메뉴 항목 즉시 반영"

steps_hint:
  - "React Router 설정 (/, /write, /review, /risk, /login)"
  - "AuthContext 생성 (Mock User 정보 제공)"
  - "Layout 컴포넌트 작성 (Header, Sidebar, Content, Footer/Nav)"
  - "RoleGuard 컴포넌트 작성 (권한 없는 페이지 접근 차단)"

preconditions:
  - "Vite + React + Tailwind 프로젝트가 초기화되어 있어야 함"

postconditions:
  - "앱 실행 시 레이아웃이 깨지지 않고, 네비게이션을 통해 각 더미 페이지로 이동 가능하다."

dependencies: []
parallelizable: true
estimated_effort: "S"
priority: "Must"
agent_profile: ["frontend"]
```

