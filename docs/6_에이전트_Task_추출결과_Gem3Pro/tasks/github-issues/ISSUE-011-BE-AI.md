---
title: "[BE] AI 도메인: 위험도 분석 및 개선안 제안"
labels: ["backend", "ai", "feature"]
assignees: []
---

## 1. 배경 (Background)
작업 내용을 바탕으로 LLM을 활용하여 잠재적 위험 요인을 분석하고 개선 대책을 제안받습니다.
관련 문서: `REQ-FUNC-06`

## 2. 상세 작업 (Tasks)
- [ ] **External API Integration**
    - [ ] OpenAI API (또는 기타 LLM) Client 설정 (`WebClient` or `OpenFeign`)
    - [ ] Prompt Engineering: 안전 분야 전문 프롬프트 템플릿 작성
- [ ] **Domain Modeling (`domain/ai`)**
    - [ ] `AiAnalysisResult` (분석 결과 저장)
- [ ] **Service Layer**
    - [ ] 비동기 처리: 분석 요청 시 즉시 응답하고, 백그라운드에서 분석 수행 (Event Driven)
- [ ] **API Implementation**
    - [ ] `POST /api/ai/analyze`: 분석 요청

## 3. 완료 조건 (Acceptance Criteria)
- [ ] 체크리스트 내용을 전송하면 AI가 분석한 위험 요인 텍스트가 반환되어야 한다.
- [ ] API Key 등 민감 정보가 소스코드에 노출되지 않아야 한다.

