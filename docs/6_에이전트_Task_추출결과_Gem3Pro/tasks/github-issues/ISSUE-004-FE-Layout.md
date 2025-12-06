---
title: "[FE] 프로젝트 셋업 및 메인 레이아웃 (PoC)"
labels: ["frontend", "feature"]
assignees: []
---

## 1. 배경 (Background)
앱의 기본 골격인 레이아웃을 구성하고, 사용자 역할(Worker/Supervisor/Manager)에 따라 메뉴가 동적으로 변경되는지 검증해야 합니다.
관련 문서: `EPIC0-FE-001`

## 2. 상세 작업 (Tasks)
- [x] **Routing 설정**
    - [x] React Router 설치 및 라우트 정의 (`/`, `/login`, `/write`, `/dashboard`)
    - [x] `PrivateRoute` (또는 Guard) 컴포넌트: 로그인 여부 체크
- [x] **Layout 컴포넌트**
    - [x] Header (GNB): 로고, 사용자 정보, 로그아웃 버튼
    - [x] Sidebar (Desktop) / Bottom Nav (Mobile)
    - [x] Responsive Design 적용 (TailwindCSS Breakpoints)
- [x] **Mock Auth Context**
    - [x] 백엔드 연동 전, 로그인 상태와 Role을 시뮬레이션하는 Context 구현
    - [x] 역할 변경 버튼을 만들어 메뉴 노출 변화 테스트

## 3. 완료 조건 (Acceptance Criteria)
- [x] 모바일 화면에서 하단 탭바가, PC 화면에서 사이드바가 보여야 한다.
- [x] '작업자' 역할 선택 시 '관리자 대시보드' 메뉴가 숨겨져야 한다.
