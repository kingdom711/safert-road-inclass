---
title: "[BE] AI 도메인: Gemini API 연동 위험도 분석"
labels: ["backend", "ai", "feature", "external-api"]
assignees: []
---

## 1. 배경 (Background)
Google Gemini API를 호출하여 체크리스트 및 작업 사진의 위험 요인을 분석합니다.
관련 문서: `REQ-FUNC-06`

---

## 2. 상세 작업 (Tasks)

### 2.1 Gemini API Client 구현
- [ ] `GeminiApiClient` 구현 (Spring WebClient 사용)
- [ ] API Key 환경변수 설정 (`GEMINI_API_KEY`)
- [ ] 프롬프트 템플릿 작성 (안전 분석용)

### 2.2 DTO 및 Service
- [ ] `AiAnalysisRequest` / `AiAnalysisResponse` DTO
- [ ] `AiAnalysisService` - Gemini API 호출 및 결과 파싱

### 2.3 API Endpoint
- [ ] `POST /api/v1/ai/analyze` - 텍스트 기반 위험 분석
- [ ] `POST /api/v1/ai/analyze-photo` - 이미지 위험 분석 (Gemini Vision)

---

## 3. 기술 스펙

### 3.1 Gemini API 호출 예시
```java
@Service
@RequiredArgsConstructor
public class GeminiApiClient {
    
    @Value("${gemini.api-key}")
    private String apiKey;
    
    private final WebClient webClient;
    
    public Mono<String> analyze(String prompt) {
        return webClient.post()
            .uri("https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=" + apiKey)
            .bodyValue(buildRequest(prompt))
            .retrieve()
            .bodyToMono(GeminiResponse.class)
            .map(this::extractText);
    }
}
```

### 3.2 환경 설정
```properties
# application.properties
gemini.api-key=${GEMINI_API_KEY:your-api-key-here}
gemini.model=gemini-pro
gemini.vision-model=gemini-pro-vision
```

### 3.3 프롬프트 템플릿
```text
당신은 산업안전 전문가입니다. 다음 체크리스트 내용을 분석하여 
위험 요인과 개선 대책을 JSON 형식으로 응답하세요.

[체크리스트 내용]
{checklist_content}

[응답 형식]
{
  "riskLevel": "LOW|MEDIUM|HIGH|CRITICAL",
  "riskFactors": ["위험요인1", "위험요인2"],
  "recommendations": ["개선대책1", "개선대책2"]
}
```

---

## 4. 완료 조건 (Acceptance Criteria)
- [ ] 체크리스트 내용 전송 시 위험 분석 결과가 JSON으로 반환된다.
- [ ] API Key가 소스코드에 노출되지 않는다 (환경변수 사용).
- [ ] API 호출 실패 시 적절한 에러 핸들링이 된다.

---

## 5. 의존성
- REQ-FUNC-03 (체크리스트 작성 기능)
- Gemini API Key 발급

## 6. 예상 소요시간
- 예상: 4시간 (API 연동 2h + 프롬프트 최적화 2h)
