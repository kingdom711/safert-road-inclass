---
title: "[BE] Template 도메인: 표준 점검표 관리"
labels: ["backend", "feature"]
assignees: []
---

## 1. 배경 (Background)
작업자가 작성할 체크리스트의 기준이 되는 '표준 점검표 템플릿'을 관리해야 합니다.
관련 문서: `REQ-FUNC-02`

## 2. 상세 작업 (Tasks)
- [ ] **Domain Modeling (`domain/template`)**
    - [ ] `ChecklistTemplate` (제목, 설명)
    - [ ] `TemplateItem` (점검 항목 내용, 필수 여부)
- [ ] **Data Seeding**
    - [ ] 애플리케이션 시작 시 기본 안전 점검표 데이터 자동 생성 (CommandLineRunner 활용)
- [ ] **API Implementation**
    - [ ] `GET /api/templates`: 템플릿 목록 조회
    - [ ] `GET /api/templates/{id}`: 특정 템플릿 및 항목 상세 조회

## 3. 완료 조건 (Acceptance Criteria)
- [ ] API 호출 시, 사전에 정의된 안전 점검 항목 리스트가 JSON으로 반환되어야 한다.

