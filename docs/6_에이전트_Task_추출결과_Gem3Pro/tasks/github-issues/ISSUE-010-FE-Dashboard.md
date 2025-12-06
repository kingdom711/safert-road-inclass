---
title: "[FE] 대시보드 및 위험 현황 시각화"
labels: ["frontend", "feature"]
assignees: []
---

## 1. 배경 (Background)
전체 현장의 위험 현황, 금일 점검 진행률 등을 한눈에 볼 수 있는 관리자용 대시보드를 구현합니다.
관련 문서: `EPIC0-FE-003`, `REQ-FUNC-05`

## 2. 상세 작업 (Tasks)
- [ ] **Dashboard Widgets**
    - [ ] 금일 점검 실시율 (차트 라이브러리 활용 - Recharts 등)
    - [ ] 고위험 요인 Top 5 리스트
- [ ] **Risk Status Page**
    - [ ] 위험성 평가 진행 현황 테이블 (필터링, 정렬 포함)
    - [ ] 상세 모달: 평가 내용 조회 및 승인 처리 버튼

## 3. 완료 조건 (Acceptance Criteria)
- [ ] 관리자로 로그인 시 메인 화면에 대시보드가 보여야 한다.
- [ ] 차트 데이터가 백엔드 API 데이터와 일치해야 한다.

