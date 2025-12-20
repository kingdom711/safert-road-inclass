---
title: "[BE] AI 도메인: GEMS AI 위험 분석 API"
labels: ["backend", "ai", "feature", "external-api"]
assignees: []
---

## 1. 배경 (Background)
GEMS AI API를 통해 체크리스트 및 작업 사진의 위험 요인을 분석합니다.
프론트엔드 가이드(`BACKEND_INTEGRATION_GUIDE.md`) 형식에 맞춰 구현되었습니다.

관련 문서: `REQ-FUNC-06`

---

## 2. 구현된 API 엔드포인트

| Method | Endpoint | 설명 | 상태 |
|--------|----------|------|------|
| `POST` | `/api/v1/business-plan/generate` | 위험 분석 요청 | ✅ 완료 |
| `GET` | `/api/v1/business-plan/history` | 분석 기록 조회 | ✅ 완료 |
| `GET` | `/api/v1/business-plan/health` | 서비스 상태 확인 | ✅ 완료 |

---

## 3. Request/Response 형식

### 3.1 위험 분석 요청

**Request:**
```json
{
  "inputType": "TEXT",
  "inputText": "건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다...",
  "photoId": null,
  "context": {
    "workType": "construction",
    "location": "2층 비계",
    "workerCount": 3,
    "currentTask": "철골 용접 작업"
  }
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "riskFactor": "고소 작업 중 안전대 미체결",
    "remediationSteps": [
      "즉시 작업을 중단하고 안전한 장소로 이동하십시오.",
      "안전대 및 부속품의 상태를 점검하십시오.",
      "안전대 체결 후 2인 1조로 작업을 재개하십시오."
    ],
    "referenceCode": "KOSHA-G-2023-01",
    "actionRecordId": "550e8400-e29b-41d4-a716-446655440000",
    "riskLevel": "HIGH",
    "analysisId": "analysis-2024-12-17-001",
    "analyzedAt": "2024-12-17T10:30:00.000Z"
  }
}
```

---

## 4. 구현 파일 목록

```
backend/src/main/java/com/jinsung/safety_road_inclass/domain/ai/
├── controller/
│   ├── AiController.java           ← 레거시 (유지)
│   └── BusinessPlanController.java ← 프론트엔드 연동용 (신규)
├── dto/
│   ├── AiAnalysisRequest.java      ← 레거시
│   ├── AiAnalysisResponse.java     ← 레거시
│   ├── BusinessPlanRequest.java    ← 프론트엔드 형식 (신규)
│   └── BusinessPlanResponse.java   ← 프론트엔드 형식 (신규)
└── service/
    ├── AiAnalysisService.java      ← 레거시
    └── BusinessPlanService.java    ← KOSHA 기반 Mock (신규)
```

---

## 5. Mock 응답 시나리오

입력 텍스트에 따라 다른 KOSHA 코드 기반 응답을 반환합니다:

| 키워드 | 위험 요인 | KOSHA 코드 | 위험도 |
|--------|----------|------------|--------|
| 안전대, 추락, 고소 | 고소 작업 중 안전대 미체결 | KOSHA-G-2023-01 | HIGH |
| 화기, 용접, 가연 | 가연성 물질 주변 화기 작업 | KOSHA-M-2023-05 | CRITICAL |
| 밀폐, 산소, 질식 | 밀폐공간 산소 농도 미확인 | KOSHA-S-2023-03 | CRITICAL |
| 기타 | 안전난간 불안정 및 추락 위험 | KOSHA-C-2023-08 | HIGH |

---

## 6. 완료 조건 (Acceptance Criteria)

- [x] 프론트엔드 가이드 형식의 API 엔드포인트 제공
- [x] `inputType`, `inputText`, `context` 필드 지원
- [x] `riskFactor`, `remediationSteps`, `referenceCode` 응답 형식
- [x] KOSHA 코드 기반 Mock 데이터 반환
- [x] Swagger UI에서 테스트 가능

---

## 7. 추후 작업

- [ ] Gemini API 연동 (API Key 발급 후)
- [ ] 분석 로그 DB 저장 (`gems_analysis_logs` 테이블)
- [ ] 이미지 분석 기능 (`photoId` 지원)

---

## 8. 테스트 방법

**Swagger UI:** http://localhost:8080/swagger-ui.html

1. **Business Plan (GEMS AI)** 섹션 펼치기
2. `POST /api/v1/business-plan/generate` 클릭
3. **Try it out** 버튼 클릭
4. Request Body 입력 후 **Execute**
