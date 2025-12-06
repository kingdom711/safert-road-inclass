---
title: "[BE] Notification 도메인: 알림 서비스"
labels: ["backend", "feature"]
assignees: []
---

## 1. 배경 (Background)
주요 상태 변경(승인, 반려, 고위험 발견) 시 사용자에게 알림을 전송합니다.
관련 문서: `REQ-FUNC-07`

## 2. 상세 작업 (Tasks)
- [ ] **Domain Modeling (`domain/notification`)**
    - [ ] `Notification` (수신자, 메시지, 읽음 여부, 링크)
- [ ] **Service Layer**
    - [ ] SSE(Server-Sent Events) 또는 WebSocket 기반 실시간 알림 구독 구현
    - [ ] 알림 생성 이벤트 리스너
- [ ] **API Implementation**
    - [ ] `GET /api/notifications`: 알림 목록 조회
    - [ ] `GET /api/notifications/subscribe`: SSE 연결

## 3. 완료 조건 (Acceptance Criteria)
- [ ] 체크리스트가 반려되었을 때 작성자에게 알림이 가야 한다.
- [ ] 알림 목록 조회 시 읽지 않은 알림이 구분되어야 한다.

