---
title: "[BE] Risk 도메인: 위험성 평가 및 대책 수립"
labels: ["backend", "feature", "advanced"]
assignees: []
---

## 1. 배경 (Background)
체크리스트에서 발견된 위험 요인(Risk)에 대해 빈도/강도를 평가하고 개선 대책을 수립하는 기능입니다.
관련 문서: `REQ-FUNC-05`

## 2. 상세 작업 (Tasks)
- [ ] **Domain Modeling (`domain/risk`)**
    - [ ] `RiskAssessment` (빈도, 강도, 위험도 점수)
    - [ ] `Countermeasure` (개선 대책 내용)
- [ ] **Service Layer**
    - [ ] 위험도 계산 로직 (빈도 x 강도 = 위험성)
    - [ ] `Checklist` 상태 업데이트 연동 (위험성 평가 완료 상태)
- [ ] **API Implementation**
    - [ ] `GET /api/risks/pending`: 위험성 평가 대상 목록 조회
    - [ ] `POST /api/risks/{id}/assess`: 평가 및 대책 저장

## 3. 완료 조건 (Acceptance Criteria)
- [ ] '아니오'로 체크된 항목들이 평가 대상 목록에 조회되어야 한다.
- [ ] 빈도와 강도를 입력하면 위험도 점수가 자동 계산되어 저장되어야 한다.

