---
title: "[FE] 체크리스트 작성 폼 UI 및 연동"
labels: ["frontend", "feature"]
assignees: []
---

## 1. 배경 (Background)
작업자가 실제로 점검 내용을 입력하고 사진을 첨부하는 모바일 친화적인 UI를 개발합니다.
관련 문서: `EPIC0-FE-002`, `REQ-FUNC-03`

## 2. 상세 작업 (Tasks)
- [ ] **Form Components**
    - [ ] O/X 선택 버튼 컴포넌트
    - [ ] 파일 업로드 컴포넌트 (미리보기 기능 포함)
    - [ ] 현장 정보 입력 인풋
- [ ] **API Integration**
    - [ ] 템플릿 조회 API 호출 및 동적 폼 렌더링
    - [ ] `FormData` 객체를 사용하여 이미지와 JSON 데이터 전송
- [ ] **Validation**
    - [ ] 필수 항목 미입력 시 제출 방지

## 3. 완료 조건 (Acceptance Criteria)
- [ ] 모바일 화면에서 사진 촬영 또는 선택 후 미리보기가 되어야 한다.
- [ ] 제출 버튼 클릭 시 백엔드 API로 데이터가 전송되고 성공 메시지가 떠야 한다.

