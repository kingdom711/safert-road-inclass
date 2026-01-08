# GEMS AI - Gemini API 연동 가이드

> **문서 상태**: ✅ 연동 완료  
> **최종 업데이트**: 2026-01-08  
> **대상 기능**: RiskSolutionPage의 "안전 지능 시스템" AI 분석 기능

---

## 📋 목차

1. [시스템 개요](#1-시스템-개요)
2. [아키텍처 흐름](#2-아키텍처-흐름)
3. [API 인터페이스](#3-api-인터페이스)
4. [프론트엔드 구현](#4-프론트엔드-구현)
5. [백엔드 구현](#5-백엔드-구현)
6. [Gemini API 연동](#6-gemini-api-연동)
7. [설정 및 환경변수](#7-설정-및-환경변수)
8. [테스트 방법](#8-테스트-방법)
9. [트러블슈팅](#9-트러블슈팅)

---

## 1. 시스템 개요

### 기능 설명

사용자가 RiskSolutionPage에서 위험 상황을 텍스트로 입력하면, AI가 분석하여 다음 정보를 반환합니다:

- **위험 요인 (riskFactor)**: 핵심 위험 요인 한 문장
- **위험 수준 (riskLevel)**: CRITICAL, HIGH, MEDIUM, LOW
- **조치 방안 (remediationSteps)**: 3~5개의 단계별 지침
- **참조 코드 (referenceCode)**: 관련 KOSHA 가이드 코드

### 사용자 인터페이스

```
프론트엔드 RiskSolutionPage
├── textarea (위험 상황 입력)
│   └── placeholder: "건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다..."
├── "🔍 AI 솔루션 요청" 버튼
└── 결과 표시 영역 (GEMSResultCard)
```

---

## 2. 아키텍처 흐름

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           프론트엔드 (React)                              │
├─────────────────────────────────────────────────────────────────────────┤
│  RiskSolutionPage.jsx                                                    │
│       │                                                                  │
│       ▼                                                                  │
│  geminiService.js (래퍼)                                                 │
│       │                                                                  │
│       ▼                                                                  │
│  gemsApi.js                                                              │
│       │ POST /api/v1/business-plan/generate                              │
└───────┼─────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           백엔드 (Spring Boot)                           │
├─────────────────────────────────────────────────────────────────────────┤
│  BusinessPlanController.java                                             │
│       │ @PostMapping("/generate")                                        │
│       ▼                                                                  │
│  BusinessPlanService.java                                                │
│       │ generate(request)                                                │
│       ▼                                                                  │
│  GeminiService.java                                                      │
│       │ analyzeRisk()                                                    │
│       ▼                                                                  │
│  RestTemplate → Google Gemini API                                        │
└───────┼─────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      Google Gemini API                                   │
│  https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash│
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. API 인터페이스

### 엔드포인트

```
POST /api/v1/business-plan/generate
```

### Request Body

```json
{
  "inputType": "TEXT",
  "inputText": "건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다. 작업자 3명이 해당 구역에서 철골 용접 작업을 진행 중이며, 안전대 체결 상태가 불량하여 추락 사고 위험이 매우 높은 상황입니다.",
  "photoId": null,
  "context": {
    "workType": "construction",
    "location": "2층 비계",
    "workerCount": 3,
    "currentTask": "철골 용접 작업"
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `inputType` | String | ✅ | `TEXT`, `PHOTO`, `BOTH` 중 하나 |
| `inputText` | String | ✅ | 위험 상황 설명 텍스트 |
| `photoId` | String | ❌ | 업로드된 사진 ID (이미지 분석 시) |
| `context` | Object | ❌ | 추가 컨텍스트 정보 |
| `context.workType` | String | ❌ | 작업 유형 |
| `context.location` | String | ❌ | 작업 위치 |
| `context.workerCount` | Integer | ❌ | 작업자 수 |
| `context.currentTask` | String | ❌ | 현재 수행 작업 |

### Response Body

```json
{
  "success": true,
  "data": {
    "riskFactor": "비계 안전난간 불량 및 안전대 미체결로 인한 추락 위험",
    "remediationSteps": [
      "즉시 해당 구역 작업을 중단하고 작업자를 대피시키십시오.",
      "모든 작업자의 안전대 체결 상태를 확인하고 재체결하십시오.",
      "비계 안전난간을 점검하고 불량 부위를 즉시 보수하십시오.",
      "관리감독자 입회 하에 비계 전체 안전점검을 실시하십시오.",
      "점검 완료 후 작업 재개 전 TBM을 실시하십시오."
    ],
    "referenceCode": "KOSHA-C-2023-08",
    "actionRecordId": "550e8400-e29b-41d4-a716-446655440000",
    "riskLevel": "CRITICAL",
    "analysisId": "analysis-2026-01-08-abc12345",
    "analyzedAt": "2026-01-08T10:30:00.000Z"
  },
  "error": null
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `riskFactor` | String | 핵심 위험 요인 (한 문장) |
| `riskLevel` | String | `CRITICAL`, `HIGH`, `MEDIUM`, `LOW` |
| `remediationSteps` | String[] | 조치 방안 배열 (3~5개) |
| `referenceCode` | String | KOSHA 가이드 코드 |
| `actionRecordId` | String | 조치 기록 UUID |
| `analysisId` | String | 분석 ID |
| `analyzedAt` | String | 분석 시각 (ISO 8601) |

---

## 4. 프론트엔드 구현

### 파일 구조

```
Life-game/safety-quest-game/src/
├── pages/
│   └── RiskSolutionPage.jsx    # 메인 페이지
├── components/
│   ├── RiskSolutionModal.jsx   # 모달 컴포넌트
│   └── GEMSResultCard.jsx      # 결과 표시 컴포넌트
├── api/
│   └── gemsApi.js              # API 클라이언트
├── utils/
│   └── geminiService.js        # 서비스 래퍼
└── config/
    └── environment.js          # 환경 설정
```

### 핵심 코드: RiskSolutionPage.jsx

```jsx
const handleSubmit = async () => {
    // 입력이 비어있으면 placeholder 텍스트 사용 (디버깅 모드)
    const textToSubmit = inputText.trim() || DEFAULT_RISK_TEXT;

    setStep('analyzing');
    setError(null);

    try {
        // geminiService → gemsApi → POST /api/v1/business-plan/generate
        const result = await geminiService.analyzeRisk(textToSubmit);
        
        setAnalysisResult(result);
        setStep('result');
    } catch (err) {
        setError('AI 분석 중 오류가 발생했습니다.');
        setStep('input');
    }
};
```

### 핵심 코드: gemsApi.js

```javascript
analyzeRisk: async (data) => {
    // Mock 모드 체크
    if (config.USE_MOCK) {
        return getMockResponse();
    }
    
    const requestBody = {
        inputType: data.photoId ? 'PHOTO' : 'TEXT',
        inputText: data.inputText,
        photoId: data.photoId || null,
        context: data.context || {}
    };
    
    // 실제 API 호출
    const response = await apiClient.post('/business-plan/generate', requestBody);
    
    // 응답 정규화
    return {
        success: true,
        riskFactor: response.riskFactor,
        remediationSteps: response.remediationSteps,
        referenceCode: response.referenceCode,
        riskLevel: response.riskLevel,
        // ...
    };
}
```

### 환경 설정 (environment.js)

```javascript
const config = {
    API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    USE_MOCK: import.meta.env.VITE_USE_MOCK === 'true',
    API_VERSION: 'v1',
};
```

---

## 5. 백엔드 구현

### 파일 구조

```
safert-road-inclass/src/main/java/com/jinsung/safety_road_inclass/
└── domain/
    └── ai/
        ├── controller/
        │   └── BusinessPlanController.java
        ├── dto/
        │   ├── BusinessPlanRequest.java
        │   ├── BusinessPlanResponse.java
        │   └── gemini/
        │       ├── GeminiRequest.java
        │       ├── GeminiResponse.java
        │       └── GeminiAnalysisResult.java
        ├── service/
        │   ├── BusinessPlanService.java
        │   └── GeminiService.java
        └── config/
            └── GeminiConfig.java
```

### BusinessPlanController.java

```java
@RestController
@RequestMapping("/api/v1/business-plan")
public class BusinessPlanController {
    
    private final BusinessPlanService businessPlanService;
    
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<BusinessPlanResponse>> generate(
            @RequestBody @Valid BusinessPlanRequest request) {
        
        BusinessPlanResponse response = businessPlanService.generate(request, requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

### BusinessPlanService.java

```java
@Service
public class BusinessPlanService {
    
    private final GeminiService geminiService;
    
    public BusinessPlanResponse generate(BusinessPlanRequest request, String requestId) {
        // Context 정보 추출
        String workType = request.getContext() != null ? request.getContext().getWorkType() : null;
        // ...
        
        // Gemini API 호출
        GeminiAnalysisResult analysisResult = geminiService.analyzeRisk(
            request.getInputText(),
            workType, location, workerCount, currentTask
        );
        
        // Response 생성
        return BusinessPlanResponse.builder()
            .riskFactor(analysisResult.getRiskFactor())
            .remediationSteps(analysisResult.getRemediationSteps())
            .referenceCode(analysisResult.getReferenceCode())
            .riskLevel(analysisResult.getRiskLevel())
            .analysisId("analysis-" + LocalDateTime.now().format(...))
            .analyzedAt(LocalDateTime.now())
            .build();
    }
}
```

---

## 6. Gemini API 연동

### GeminiService.java 핵심 로직

```java
@Service
public class GeminiService {
    
    // 산업안전보건 전문가 시스템 프롬프트
    private static final String SYSTEM_PROMPT = """
        당신은 산업안전보건 전문가입니다.
        사용자가 설명하는 현장 위험 상황을 분석하고, 다음 형식으로 응답하세요:

        1. riskFactor: 핵심 위험 요인 (한 문장)
        2. riskLevel: 위험 등급 (CRITICAL, HIGH, MEDIUM, LOW 중 하나)
        3. remediationSteps: 구체적인 조치 방안 (3~5개의 단계별 지침, 배열 형태)
        4. referenceCode: 관련 KOSHA 가이드 코드 (아래 목록에서 선택)

        KOSHA 코드 목록:
        - KOSHA-G-2023-01: 고소작업, 안전대 관련
        - KOSHA-M-2023-05: 화기작업, 화재 예방
        - KOSHA-P-2023-12: 보호구, 개인보호구 착용
        - KOSHA-C-2023-08: 가설구조, 비계 및 거푸집
        - KOSHA-S-2023-03: 밀폐공간, 밀폐공간 작업
        - KOSHA-E-2023-07: 전기작업, 전기 안전
        - KOSHA-L-2023-11: 양중작업, 크레인 및 양중기
        - KOSHA-F-2023-04: 화재예방, 용접 화재 감시

        반드시 위 4가지 필드만 JSON 형식으로 응답하세요.
        """;
    
    public GeminiAnalysisResult analyzeRisk(String situationText, ...) {
        String userPrompt = buildUserPrompt(situationText, ...);
        
        try {
            GeminiResponse response = callGeminiApi(userPrompt);
            return parseGeminiResponse(response);
        } catch (Exception e) {
            return createFallbackResponse(situationText);
        }
    }
}
```

### KOSHA 코드 목록

| 코드 | 분류 | 설명 |
|------|------|------|
| KOSHA-G-2023-01 | 고소작업 | 안전대 관련 |
| KOSHA-M-2023-05 | 화기작업 | 화재 예방 |
| KOSHA-P-2023-12 | 보호구 | 개인보호구 착용 |
| KOSHA-C-2023-08 | 가설구조 | 비계 및 거푸집 |
| KOSHA-S-2023-03 | 밀폐공간 | 밀폐공간 작업 |
| KOSHA-E-2023-07 | 전기작업 | 전기 안전 |
| KOSHA-L-2023-11 | 양중작업 | 크레인 및 양중기 |
| KOSHA-F-2023-04 | 화재예방 | 용접 화재 감시 |

---

## 7. 설정 및 환경변수

### 백엔드 application.properties

```properties
# Gemini API Configuration
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
gemini.api.timeout=30000

# API Key 설정 (환경변수 권장)
custom.gemini.key=${GEMINI_API_KEY:YOUR_API_KEY_HERE}

# 또는 직접 설정 (개발용)
custom.gemini.key=AIzaSyDl8iM56ICVcVg7-NF9ZC55unsKP3mXOu8
```

### 프론트엔드 .env

```env
# 백엔드 서버 URL
VITE_API_BASE_URL=http://localhost:8080

# Mock 모드 (백엔드 없이 테스트)
VITE_USE_MOCK=false
```

### 환경변수 설정 방법

```bash
# Windows (PowerShell)
$env:GEMINI_API_KEY="YOUR_API_KEY"

# Windows (영구 설정)
setx GEMINI_API_KEY "YOUR_API_KEY"

# Linux/Mac
export GEMINI_API_KEY="YOUR_API_KEY"
```

---

## 8. 테스트 방법

### 8.1 Swagger UI 테스트

1. 백엔드 서버 실행: `./gradlew bootRun`
2. Swagger UI 접속: http://localhost:8080/swagger-ui/index.html
3. `POST /api/v1/business-plan/generate` 선택
4. 예시 요청으로 테스트

### 8.2 cURL 테스트

```bash
curl -X POST http://localhost:8080/api/v1/business-plan/generate \
  -H "Content-Type: application/json" \
  -d '{
    "inputType": "TEXT",
    "inputText": "건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다."
  }'
```

### 8.3 프론트엔드 테스트

1. 백엔드 서버 실행: `cd safert-road-inclass && ./gradlew bootRun`
2. 프론트엔드 실행: `cd Life-game/safety-quest-game && npm run dev`
3. http://localhost:3000 접속
4. 로그인 → Dashboard → 위험 분석 메뉴
5. 위험 상황 입력 후 "AI 솔루션 요청" 클릭

### 8.4 디버깅 모드

프론트엔드 RiskSolutionPage에서:
- **빈 칸으로 제출**: 기본 placeholder 텍스트로 테스트됨
- 콘솔에서 `[GEMS API]` 로그 확인

---

## 9. 트러블슈팅

### 문제: "서버에 연결할 수 없습니다"

**원인**: 백엔드 서버가 실행되지 않음

**해결**:
```bash
cd safert-road-inclass
./gradlew bootRun
```

### 문제: Mock 응답만 반환됨

**원인**: 프론트엔드 Mock 모드 활성화

**해결**: `.env` 파일에서 `VITE_USE_MOCK=false` 설정

### 문제: Gemini API 오류

**원인**: API Key 문제 또는 할당량 초과

**해결**:
1. API Key 확인: `application.properties`의 `custom.gemini.key`
2. Google AI Studio에서 할당량 확인
3. Fallback 응답이 반환되는지 로그 확인

### 문제: CORS 오류

**원인**: 프론트엔드-백엔드 도메인 불일치

**해결**: `SecurityConfig.java`의 `corsConfigurationSource()`에서 허용 도메인 확인
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:5173"
));
```

---

## 📊 관련 문서

- [API 명세](./API_SPECIFICATION.md)
- [백엔드 통합 가이드](./BACKEND_INTEGRATION_GUIDE.md)
- [Gemini API 테스트 리포트](./GEMINI_API_TEST_REPORT.md)

---

*문서 작성일: 2024-12-20*  
*최종 업데이트: 2026-01-08*  
*버전: 2.0.0 (연동 완료)*
