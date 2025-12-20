# REQ-FUNC-06: GEMS AI 기반 위험 분석

## 1. 개요
- **목적**: GEMS AI API를 통한 체크리스트/사진 위험도 분석
- **SRS 섹션**: 3.6 F-06
- **방식**: 프론트엔드 가이드 형식에 맞춘 API 제공

---

## 2. API 명세

### 2.1 위험 분석 요청
```
POST /api/v1/business-plan/generate
Content-Type: application/json
Authorization: Bearer {token}

Request:
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

Response:
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

### 2.2 분석 기록 조회
```
GET /api/v1/business-plan/history
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": [
    {
      "analysisId": "analysis-2025-12-17-abc12345",
      "riskFactor": "고소 작업 중 안전대 미체결",
      "riskLevel": "HIGH",
      "referenceCode": "KOSHA-G-2023-01",
      "analyzedAt": "2025-12-17T18:00:00"
    }
  ]
}
```

### 2.3 서비스 상태 확인
```
GET /api/v1/business-plan/health

Response:
{
  "success": true,
  "data": "GEMS AI Service is running (Mock Mode)"
}
```

---

## 3. Request 필드 설명

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `inputType` | String | ✅ | 입력 유형: `TEXT`, `PHOTO`, `BOTH` |
| `inputText` | String | ✅ | 분석할 텍스트 내용 |
| `photoId` | String | ❌ | 이미지 분석 시 사진 ID |
| `context.workType` | String | ❌ | 작업 유형 (construction, electrical 등) |
| `context.location` | String | ❌ | 작업 위치 |
| `context.workerCount` | Integer | ❌ | 작업자 수 |
| `context.currentTask` | String | ❌ | 현재 수행 중인 작업 |

---

## 4. Response 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `riskFactor` | String | 식별된 위험 요인 (단수형) |
| `remediationSteps` | String[] | 조치 단계 목록 |
| `referenceCode` | String | KOSHA 참조 코드 |
| `actionRecordId` | String | 조치 기록 ID (UUID) |
| `riskLevel` | String | 위험 수준: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `analysisId` | String | 분석 ID |
| `analyzedAt` | DateTime | 분석 일시 |

---

## 5. KOSHA 참조 코드

| 코드 | 분류 | 설명 |
|------|------|------|
| `KOSHA-G-2023-01` | 고소작업 | 안전대 관련 기준 |
| `KOSHA-M-2023-05` | 화기작업 | 화재 예방 기준 |
| `KOSHA-C-2023-08` | 가설구조 | 비계 및 거푸집 기준 |
| `KOSHA-S-2023-03` | 밀폐공간 | 밀폐공간 작업 기준 |
| `KOSHA-E-2023-07` | 전기작업 | 전기 안전 기준 |
| `KOSHA-P-2023-12` | 보호구 | 개인보호구 착용 기준 |

---

## 6. 구현 상태

```yaml
task_id: "REQ-FUNC-06-AI-ANALYSIS"
title: "GEMS AI 기반 위험 분석 API"
type: "functional"
component: ["backend"]
estimated_effort: "S"
priority: "Should"

implementation_status:
  api_endpoint: "✅ 완료"
  mock_response: "✅ 완료"
  gemini_integration: "⏳ 대기 (API Key 필요)"

tasks:
  - "✅ BusinessPlanController 구현"
  - "✅ BusinessPlanService Mock 구현"
  - "✅ BusinessPlanRequest/Response DTO"
  - "⏳ GeminiApiClient 구현 (추후)"
  - "⏳ 분석 로그 DB 저장 (추후)"

dependencies: 
  - "프론트엔드 연동 테스트"
  - "Gemini API Key 발급"
```

---

## 7. 완료 조건

- [x] `POST /api/v1/business-plan/generate` 엔드포인트 동작
- [x] `GET /api/v1/business-plan/history` 엔드포인트 동작
- [x] 프론트엔드 가이드 형식의 Request/Response
- [x] Mock 데이터로 KOSHA 코드 기반 응답 반환
- [ ] 실제 Gemini API 연동 (추후)
- [ ] 분석 로그 DB 저장 (추후)
