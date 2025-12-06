---
title: "[BE] Checklist 도메인: 작성 및 저장 구현"
labels: ["backend", "feature", "core"]
assignees: []
---

## 1. 배경 (Background)
작업자가 현장에서 입력한 점검 결과(O/X)와 사진을 저장하는 핵심 기능입니다.
관련 문서: `REQ-FUNC-03`

## 2. 상세 작업 (Tasks)
- [ ] **Domain Modeling (`domain/checklist`)**
    - [ ] `Checklist` (작성자, 날짜, 현장명, 상태)
    - [ ] `ChecklistItem` (개별 항목 응답, 예/아니오)
- [ ] **File Upload Service**
    - [ ] `StorageService` 인터페이스 정의
    - [ ] Local Storage 구현체 (개발용): 이미지를 로컬 폴더에 저장
- [ ] **Service Layer**
    - [ ] `ChecklistService.create()`: 템플릿 기반으로 인스턴스 생성 및 응답 저장
    - [ ] '아니오' 응답 시 Risk Flag 자동 설정 로직
- [ ] **API Implementation**
    - [ ] `POST /api/checklists`: 점검표 제출 (JSON + MultipartFile)
    - [ ] `GET /api/checklists`: 내 점검표 목록 조회

## 3. 완료 조건 (Acceptance Criteria)
- [ ] 사진 파일과 함께 체크리스트 데이터가 DB에 정상적으로 저장되어야 한다.
- [ ] 저장된 이미지를 조회할 수 있는 URL이 제공되어야 한다.

