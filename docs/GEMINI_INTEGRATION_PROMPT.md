# GEMS AI - Gemini API 연동 프롬프트

> **용도**: 백엔드 개발자가 Cursor AI에서 사용할 프롬프트입니다.
> **대상 기능**: RiskSolutionPage의 "안전 지능 시스템" AI 분석 기능

---

## 📋 사용 방법

1. 백엔드 프로젝트를 Cursor에서 열기
2. 아래 프롬프트를 Cursor Chat에 붙여넣기
3. AI가 생성한 코드를 검토 후 적용

---

## 🤖 Cursor 프롬프트

아래 내용을 복사하여 백엔드 Cursor에 붙여넣으세요:

```
### 1. 배경 및 미션
아래 [기존 소스코드]는 프론트엔드(RiskSolutionPage)에서 위험 상황 텍스트를 입력받아 
AI가 위험 요인 분석 및 조치 방안을 반환하는 Spring Boot 백엔드 로직입니다.

현재 구현된 FE-BE 간의 요청(Request) 및 응답(Response) 포맷을 '절대적으로 유지'한 상태에서, 
내부 Mock 로직을 **Google Gemini API 직접 호출**로 대체해주세요.

---

### 2. 프론트엔드 API 인터페이스 (변경 불가)

#### 엔드포인트
POST /api/v1/business-plan/generate

#### Request Body
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

#### Response Body (이 구조 반드시 유지)
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

---

### 3. 상세 구현 요구사항

- **인터페이스 준수**: 
    - 기존 `BusinessPlanController`의 엔드포인트(`/api/v1/business-plan/generate`) 유지
    - `BusinessPlanRequest`, `BusinessPlanResponse` DTO 필드 구조 그대로 유지
    
- **Gemini API 직접 호출**: 
    - `RestTemplate` 또는 `WebClient`를 사용하여 Google Gemini API 직접 호출
    - Gemini API 엔드포인트: `https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent`
    - API Key: `AIzaSyBkBFQVLIpDmpDu0aRsT8h-N0QuV1jsqHE`
    - API Key는 `application.properties`에서 `gemini.api.key`로 관리
    
- **시스템 프롬프트 (안전 전문가 역할 부여)**:

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

- **Gemini 응답 파싱**:
    - Gemini의 텍스트 응답에서 JSON을 추출하여 DTO 필드에 매핑
    - 파싱 실패 시 기본 응답 반환 (Fallback 처리)

- **토큰 측정 및 로깅**:
    - Gemini 응답의 `usageMetadata`에서 토큰 정보 추출
    - `[Gemini Usage Log] Input: {promptTokens}, Output: {candidatesTokens}, Total: {totalTokens}` 형식으로 로그
    
- **응답 확장 (Optional)**:
    - 기존 Response 구조 유지하면서 `usage` 객체를 추가할 수 있음:
{
  "success": true,
  "data": { ... },
  "usage": {
    "promptTokens": 150,
    "candidatesTokens": 200,
    "totalTokens": 350
  }
}

---

### 4. [기존 소스코드]

#### BusinessPlanController.java
@RestController
@RequestMapping("/api/v1/business-plan")
@RequiredArgsConstructor
public class BusinessPlanController {
    
    private final BusinessPlanService businessPlanService;
    
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<BusinessPlanResponse>> generate(
            @RequestBody BusinessPlanRequest request) {
        
        BusinessPlanResponse response = businessPlanService.analyzeRisk(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

#### BusinessPlanRequest.java
@Data
public class BusinessPlanRequest {
    private String inputType;  // TEXT, PHOTO, BOTH
    private String inputText;
    private String photoId;
    private Map<String, Object> context;
}

#### BusinessPlanResponse.java
@Data
@Builder
public class BusinessPlanResponse {
    private String riskFactor;
    private List<String> remediationSteps;
    private String referenceCode;
    private String actionRecordId;
    private String riskLevel;
    private String analysisId;
    private LocalDateTime analyzedAt;
}

#### BusinessPlanService.java (현재 Mock 구현)
@Service
@RequiredArgsConstructor
public class BusinessPlanService {
    
    public BusinessPlanResponse analyzeRisk(BusinessPlanRequest request) {
        // TODO: 현재 Mock 로직을 Gemini API 호출로 대체
        return BusinessPlanResponse.builder()
            .riskFactor("고소 작업 중 안전대 미체결")
            .remediationSteps(List.of(
                "즉시 작업을 중단하고 안전한 장소로 이동하십시오.",
                "안전대 및 부속품의 상태를 점검하십시오."
            ))
            .referenceCode("KOSHA-G-2023-01")
            .riskLevel("HIGH")
            .analysisId("analysis-" + System.currentTimeMillis())
            .analyzedAt(LocalDateTime.now())
            .build();
    }
}

---

### 5. 출력 형식

다음 파일들을 작성해주세요:

1. **GeminiService.java** - Gemini API 호출 및 응답 파싱 담당
2. **GeminiConfig.java** - API Key 및 설정 관리
3. **GeminiRequest.java / GeminiResponse.java** - Gemini API용 DTO
4. **BusinessPlanService.java** - Gemini 연동으로 수정된 전체 코드
5. **application.properties 추가 항목**:
   - gemini.api.key=AIzaSyBkBFQVLIpDmpDu0aRsT8h-N0QuV1jsqHE
   - gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
   - gemini.api.timeout=30000
```

---

## 📊 핵심 변경 사항 요약

| 항목 | 원본 예시 | 프로젝트 맞춤 수정 |
|------|----------|------------------|
| Controller | `{{Controller명}}` | `BusinessPlanController` |
| 엔드포인트 | `/api/gems/analyze-risk` | `/api/v1/business-plan/generate` |
| Request DTO | `GemsAnalysisRequest` | `BusinessPlanRequest` |
| Response DTO | `GemsAnalysisResponse` | `BusinessPlanResponse` |
| AI 엔진 | Spring AI ChatModel | Gemini API 직접 호출 (RestTemplate/WebClient) |
| 도메인 | 문서 보강 | 산업안전 위험 분석 |
| 시스템 프롬프트 | 문서 편집자 | 산업안전보건 전문가 |

---

## 🔗 관련 파일 참조

### 프론트엔드 (현재 프로젝트)
- API 클라이언트: `src/api/gemsApi.js`
- 페이지 컴포넌트: `src/pages/RiskSolutionPage.jsx`
- 서비스 래퍼: `src/utils/geminiService.js`

### 백엔드 프로젝트 구조 (예상)
```
backend/src/main/java/com/jinsung/safety_road_inclass/
├── domain/
│   └── ai/
│       ├── controller/
│       │   └── BusinessPlanController.java
│       ├── dto/
│       │   ├── BusinessPlanRequest.java
│       │   └── BusinessPlanResponse.java
│       └── service/
│           ├── BusinessPlanService.java
│           ├── GeminiService.java        # 신규
│           └── GeminiConfig.java         # 신규
```

---

## ⚙️ application.properties 추가 설정

```properties
# Gemini API Configuration
gemini.api.key=AIzaSyBkBFQVLIpDmpDu0aRsT8h-N0QuV1jsqHE
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
gemini.api.timeout=30000
```

> ⚠️ **보안 주의**: 프로덕션 환경에서는 API Key를 환경변수로 관리하세요.
> `gemini.api.key=${GEMINI_API_KEY}`

---

## 🧪 테스트 시나리오

### 입력 예시
```
건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다. 
작업자 3명이 해당 구역에서 철골 용접 작업을 진행 중이며, 
안전대 체결 상태가 불량하여 추락 사고 위험이 매우 높은 상황입니다.
```

### 예상 출력
```json
{
  "riskFactor": "비계 안전난간 불량 및 안전대 미체결로 인한 추락 위험",
  "riskLevel": "CRITICAL",
  "remediationSteps": [
    "즉시 해당 구역 작업을 중단하고 작업자를 대피시키십시오.",
    "모든 작업자의 안전대 체결 상태를 확인하고 재체결하십시오.",
    "비계 안전난간을 점검하고 불량 부위를 즉시 보수하십시오.",
    "관리감독자 입회 하에 비계 전체 안전점검을 실시하십시오.",
    "점검 완료 후 작업 재개 전 TBM(Tool Box Meeting)을 실시하십시오."
  ],
  "referenceCode": "KOSHA-C-2023-08"
}
```

---

*문서 작성일: 2024-12-20*
*버전: 1.0.0*

