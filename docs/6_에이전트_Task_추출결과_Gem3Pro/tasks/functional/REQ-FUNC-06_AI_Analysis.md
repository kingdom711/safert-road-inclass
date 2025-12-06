# REQ-FUNC-06: AI 작업사진 위험분석

## 1. 개요
- **목적**: 업로드된 사진을 AI로 분석하여 위험도 태깅.
- **SRS 섹션**: 3.6 F-06
- **구성 요소**:
    - BE: AI 서비스 호출 클라이언트 (Feign/RestTemplate), 비동기/동기 처리
    - AI: Gemini API 또는 Vision API 호출 래퍼
    - FE: 분석 결과 시각화

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "REQ-FUNC-06-AI-ANALYSIS"
title: "Gemini 기반 작업사진 위험도 분석 연동"
summary: >
  사진 업로드 시점에(또는 비동기로) Google Gemini API(또는 사내 Gateway)를 호출하여
  이미지의 위험도(Safe/Caution/Danger)와 태그를 추출하고 DB에 저장한다.
type: "functional"
epic: "EPIC_2_RISK_MGMT"
req_ids: ["F-06"]
component: ["backend", "ai"]

context:
  apis:
    - "POST /api/ai/analyze-photo" (Internal or External)
  entities: ["AIAnalysisResult"]

inputs:
  description: "이미지 파일 또는 S3 URL"

outputs:
  description: "Risk Level, Tags"

steps_hint:
  - "BE: Gemini Pro Vision (또는 유사 모델) API 연동 클라이언트 구현"
  - "BE: 프롬프트 엔지니어링 (안전모 미착용, 사다리 전도 위험 등 판별)"
  - "BE: 분석 결과를 AIAnalysisResult 엔티티에 저장하고 Checklist 위험 플래그 갱신"
  - "FE: 사진 옆에 분석 결과(배지, 태그) 표시 UI"

preconditions:
  - "REQ-FUNC-03-WRITE (사진 업로드 기능)"
  - "Gemini API Key 발급 및 환경변수 설정"

postconditions:
  - "사진 업로드 후 일정 시간 내에 분석 결과가 DB에 생성된다."

dependencies: ["REQ-FUNC-03-WRITE"]
parallelizable: true
estimated_effort: "M"
priority: "Should"
agent_profile: ["backend", "ml"]
```

