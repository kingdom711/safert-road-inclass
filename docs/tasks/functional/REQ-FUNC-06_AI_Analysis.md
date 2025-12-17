# REQ-FUNC-06: Gemini API 기반 위험 분석

## 1. 개요
- **목적**: Google Gemini API를 호출하여 체크리스트/사진의 위험도를 분석
- **SRS 섹션**: 3.6 F-06
- **방식**: 외부 API 호출 (자체 AI 엔진 없음)

---

## 2. API 명세

### 2.1 텍스트 분석
```
POST /api/v1/ai/analyze
Content-Type: application/json
Authorization: Bearer {token}

Request:
{
  "checklistId": 1,
  "content": "체크리스트 내용..."
}

Response:
{
  "success": true,
  "data": {
    "riskLevel": "HIGH",
    "riskFactors": ["안전모 미착용", "안전띠 미연결"],
    "recommendations": ["안전모 착용 의무화", "작업 전 안전띠 점검"]
  }
}
```

### 2.2 이미지 분석
```
POST /api/v1/ai/analyze-photo
Content-Type: multipart/form-data
Authorization: Bearer {token}

Request:
- photo: (이미지 파일)

Response:
{
  "success": true,
  "data": {
    "riskLevel": "MEDIUM",
    "detectedIssues": ["사다리 불안정", "조명 부족"],
    "tags": ["고소작업", "야간작업"]
  }
}
```

---

## 3. 구현 가이드

### 3.1 Gemini API Client
```java
@Component
public class GeminiApiClient {
    
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1";
    
    @Value("${gemini.api-key}")
    private String apiKey;
    
    private final WebClient webClient;
    
    public AiAnalysisResponse analyzeText(String content) {
        // Gemini Pro 모델 호출
    }
    
    public AiAnalysisResponse analyzeImage(byte[] imageData) {
        // Gemini Pro Vision 모델 호출
    }
}
```

### 3.2 프롬프트 전략
| 분석 유형 | 모델 | 프롬프트 포인트 |
|----------|------|----------------|
| 텍스트 분석 | gemini-pro | 안전 체크리스트 위험요인 추출 |
| 이미지 분석 | gemini-pro-vision | 안전모, 안전띠, 위험 행동 감지 |

---

## 4. 환경 설정

```properties
# application.properties
gemini.api-key=${GEMINI_API_KEY}
gemini.model.text=gemini-pro
gemini.model.vision=gemini-pro-vision
gemini.timeout=30000
```

---

## 5. Task Checklist

```yaml
task_id: "REQ-FUNC-06-AI-ANALYSIS"
title: "Gemini API 기반 위험 분석 연동"
type: "functional"
component: ["backend"]
estimated_effort: "S"
priority: "Should"

tasks:
  - "GeminiApiClient 구현 (WebClient)"
  - "AiAnalysisService 구현"
  - "POST /api/v1/ai/analyze 엔드포인트"
  - "POST /api/v1/ai/analyze-photo 엔드포인트"
  - "에러 핸들링 (API 실패, 타임아웃)"

dependencies: 
  - "REQ-FUNC-03 (체크리스트 작성)"
  - "Gemini API Key 발급"
```

---

## 6. 완료 조건
- [ ] 텍스트 분석 API가 위험도/위험요인/개선대책을 반환한다.
- [ ] 이미지 분석 API가 위험 요소를 감지하고 태그를 반환한다.
- [ ] API Key가 환경변수로 관리된다.
- [ ] API 호출 실패 시 fallback 응답을 반환한다.
