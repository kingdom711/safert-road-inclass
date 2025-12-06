---
title: "[BE] Review 도메인: 검토 및 승인 프로세스"
labels: ["backend", "feature"]
assignees: []
---

## 1. 배경 (Background)
작성된 체크리스트와 위험성 평가 결과를 관리자가 검토하고 최종 승인(결재)하는 프로세스입니다.
관련 문서: `REQ-FUNC-04`

## 2. 상세 작업 (Tasks)
- [ ] **Domain Modeling (`domain/review`)**
    - [ ] `ReviewLog` (검토자, 검토 의견, 결과-승인/반려)
- [ ] **Service Layer**
    - [ ] 상태 변경 로직 (SUBMITTED -> REVIEWED -> APPROVED/REJECTED)
    - [ ] 반려 시 알림 이벤트 발행(옵션)
- [ ] **API Implementation**
    - [ ] `POST /api/reviews/{checklistId}`: 검토 결과 등록

## 3. 완료 조건 (Acceptance Criteria)
- [ ] 관리자 권한(`ROLE_SUPERVISOR`)만 승인 API를 호출할 수 있어야 한다.
- [ ] 승인 완료 시 체크리스트의 최종 상태가 `APPROVED`가 되어야 한다.

