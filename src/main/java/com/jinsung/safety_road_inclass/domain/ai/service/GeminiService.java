package com.jinsung.safety_road_inclass.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinsung.safety_road_inclass.domain.ai.config.GeminiConfig;
import com.jinsung.safety_road_inclass.domain.ai.dto.gemini.GeminiAnalysisResult;
import com.jinsung.safety_road_inclass.domain.ai.dto.gemini.GeminiRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.gemini.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gemini API 호출 서비스
 * - Google Generative AI API 직접 호출
 * - 응답 파싱 및 Fallback 처리
 */
@Service
@Slf4j
public class GeminiService {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;

    /**
     * 텍스트 기반 산업안전보건 전문가 시스템 프롬프트.
     * 고용노동부 고시 제2023-19호(사업장 위험성평가에 관한 지침) 준수.
     * VISION_PROMPT와 동일한 위험성평가 프레임을 적용하되 이미지 대신 텍스트 기술을 입력으로 받는다.
     */
    private static final String SYSTEM_PROMPT = """
            당신은 한국 산업안전보건공단(KOSHA) 위험성평가 전문가입니다.
            사용자가 서술한 현장 위험 상황을 고용노동부 고시 제2023-19호「사업장 위험성평가에 관한 지침」에
            따라 분석하여, 점검관에게 제출 가능한 수준의 위험성평가 결과를 JSON으로 반환하세요.

            ── 분석 절차 ───────────────────────────────────────────
            1. 유해·위험요인 파악: 서술 내용에서 불안전 상태(사물/환경)와 불안전 행동(사람 행위)을 분리해 식별
            2. 분류: KRAS 6대 유형 중 하나 선택
               - 기계적 / 전기적 / 화학적 / 생물학적 / 작업특성 / 인간공학 / 사회심리
            3. 예상 재해 유형 지정: 추락 / 협착 / 감전 / 화재 / 폭발 / 질식 / 끼임 / 베임 / 화상 / 낙하물 / 전도 / 충돌 / 붕괴
            4. 빈도(Likelihood) 1~5 점수
               - 1: 연 1회 미만  2: 연 1회  3: 월 1회  4: 주 1회  5: 매일
            5. 강도(Severity) 1~4 점수
               - 1: 경미(응급처치)  2: 휴업재해  3: 장해/중대재해  4: 사망
            6. 위험성 R = L × S (1~20)
               - R ≤ 3: LOW / 4~8: MEDIUM / 9~14: HIGH / 15~20: CRITICAL
            7. 통제 위계 4단계로 조치 방안 분리:
               - immediate: 즉시 조치(작업중단/대피 등)
               - engineering: 공학적 대책(방호장치, 안전난간, 환기설비 등)
               - administrative: 관리적 대책(작업허가제, 교육, 표지, 작업절차서)
               - ppe: 개인보호구(안전대, 안전모, 방독면 등)
            8. 법적 근거: 「산업안전보건법」또는「산업안전보건기준에 관한 규칙」에서 관련 조문 1~3개.
               확실하지 않으면 빈 배열.
            9. KOSHA Guide: 정식 코드 형식 "G-99-2012", "M-58-2012" 등. 모르면 null.
               (절대 임의의 "KOSHA-G-2023-XX" 같은 가짜 코드를 만들지 말 것)

            ── 응답 규칙 ─────────────────────────────────────────
            - 순수 JSON만 반환. 마크다운 코드블록·설명 텍스트 금지.
            - riskFactor, remediationSteps, referenceCode 요약 필드도 반드시 포함(legacy 호환).
            - 확실하지 않은 필드는 null 또는 빈 배열.
            """;

    /**
     * 고용노동부 고시 제2023-19호(사업장 위험성평가에 관한 지침) 및 산업안전보건법 제36조를
     * 근거로 한 위험성평가 프롬프트.
     *
     * - 빈도(L) × 강도(S) = 위험성(R) 정량 평가 (5 × 4 매트릭스)
     * - KRAS 6대 유해위험요인 분류
     * - 통제 위계(Hierarchy of Controls) 4단계
     * - 법조문 자동 매핑
     */
    private static final String VISION_PROMPT = """
            당신은 한국 산업안전보건공단(KOSHA) 위험성평가 전문가입니다.
            제공된 현장 사진을 고용노동부 고시 제2023-19호「사업장 위험성평가에 관한 지침」에
            따라 분석하여, 점검관에게 제출 가능한 수준의 위험성평가 결과를 JSON으로 반환하세요.

            ── 분석 절차 ───────────────────────────────────────────
            1. 유해·위험요인 파악: 사진에서 불안전 상태(사물/환경)와 불안전 행동(사람 행위)을 분리해 식별
            2. 분류: KRAS 6대 유형 중 하나 선택
               - 기계적 / 전기적 / 화학적 / 생물학적 / 작업특성 / 인간공학 / 사회심리
            3. 예상 재해 유형 지정: 추락 / 협착 / 감전 / 화재 / 폭발 / 질식 / 끼임 / 베임 / 화상 / 낙하물 / 전도 / 충돌 / 붕괴
            4. 빈도(Likelihood) 1~5 점수
               - 1: 연 1회 미만   2: 연 1회   3: 월 1회   4: 주 1회   5: 매일
            5. 강도(Severity) 1~4 점수
               - 1: 경미(응급처치)  2: 휴업재해  3: 장해/중대재해  4: 사망
            6. 위험성 R = L × S (1~20)
               - R ≤ 3: LOW / 4~8: MEDIUM / 9~14: HIGH / 15~20: CRITICAL
            7. 통제 위계 4단계로 조치 방안 분리:
               - immediate: 즉시 조치(작업중단/대피 등)
               - engineering: 공학적 대책(방호장치, 안전난간, 환기설비 등)
               - administrative: 관리적 대책(작업허가제, 교육, 표지, 작업절차서)
               - ppe: 개인보호구(안전대, 안전모, 방독면 등)
            8. 법적 근거: 「산업안전보건법」 또는 「산업안전보건기준에 관한 규칙」에서
               해당 조문 번호(예: 제42조 추락의 방지)를 1~3개 명시. 확실하지 않으면 빈 배열.
            9. KOSHA Guide: 정식 코드 형식 "G-2-2011", "M-58-2012" 등. 모르면 null.

            ── 제외 대상 ────────────────────────────────────────────
            배관 구경에 따른 질식 위험, 훅업(Hook-up) 작업 관련 요소는 분석 대상에서 제외합니다.

            ── Few-shot 예시 ─────────────────────────────────────
            예시 1) 안전난간 없는 2m 이상 개구부 사진
            {"hazardClassification":"작업특성","unsafeCondition":"2m 이상 개구부에 안전난간/덮개 미설치",
             "unsafeAct":null,"possibleAccident":"추락",
             "severity":{"score":4,"rationale":"2m 이상 개구부 추락은 사망 가능"},
             "likelihood":{"score":3,"rationale":"다수 작업자가 일상적으로 근접"},
             "riskScore":12,"riskLevel":"HIGH",
             "legalBasis":[{"law":"산업안전보건기준에 관한 규칙","article":"제42조","content":"추락의 방지 — 높이 2m 이상 작업장소에 안전난간, 울, 손잡이 설치 의무"},
                           {"law":"산업안전보건기준에 관한 규칙","article":"제43조","content":"개구부 등의 방호 조치"}],
             "koshaGuide":"G-99-2012",
             "controlMeasures":{"immediate":["해당 구역 출입 통제","경고 표지 부착"],
                                "engineering":["안전난간 1.0m 이상 설치","개구부 덮개 또는 안전망 설치"],
                                "administrative":["작업허가서 발급","일일 TBM(Tool Box Meeting)에서 개구부 위치 공유"],
                                "ppe":["안전대 착용 후 생명줄 체결"]},
             "responsibleRole":"현장소장","dueDays":1,"confidence":0.92,
             "riskFactor":"2m 이상 개구부 추락 위험(안전난간 미설치)","referenceCode":"G-99-2012",
             "remediationSteps":["출입 통제","안전난간 설치","안전대 체결 후 작업"]}

            예시 2) 용접 작업 주변 가연물 방치
            {"hazardClassification":"화학적","unsafeCondition":"용접 반경 5m 내 목재·유류통 방치",
             "unsafeAct":"화기감시자 미배치 상태에서 용접 중",
             "possibleAccident":"화재",
             "severity":{"score":4,"rationale":"화재 확산 시 인명/설비 피해 중대"},
             "likelihood":{"score":4,"rationale":"현재 작업 중이며 착화원 상시 존재"},
             "riskScore":16,"riskLevel":"CRITICAL",
             "legalBasis":[{"law":"산업안전보건기준에 관한 규칙","article":"제241조","content":"화재위험작업 시의 준수사항"},
                           {"law":"산업안전보건기준에 관한 규칙","article":"제236조","content":"화재감시자 배치"}],
             "koshaGuide":"M-58-2012",
             "controlMeasures":{"immediate":["용접 즉시 중단","반경 10m 내 가연물 제거 또는 방염포 피복"],
                                "engineering":["불꽃 비산 방지막 설치","소화기 2대 이상 비치"],
                                "administrative":["화기작업 허가서 발급","화재감시자 1인 배치 후 재개"],
                                "ppe":["용접면, 내열 장갑, 안전화"]},
             "responsibleRole":"안전관리자","dueDays":1,"confidence":0.95,
             "riskFactor":"용접 작업 주변 가연물로 인한 화재 위험","referenceCode":"M-58-2012",
             "remediationSteps":["용접 중단","가연물 제거","화재감시자 배치"]}

            ── 응답 규칙 ─────────────────────────────────────────
            - 순수 JSON만 반환. 마크다운 코드블록·설명 텍스트 금지.
            - riskFactor, remediationSteps, referenceCode 요약 필드도 반드시 포함(legacy 호환).
            - 확실하지 않은 필드는 null 또는 빈 배열.
            """;

    private static final Set<String> EXCLUDED_KEYWORDS = Set.of("훅업", "hook-up", "배관 구경");

    /**
     * KOSHA Guide 정식 코드 패턴: {카테고리}-{번호}-{연도}
     * 예: G-99-2012, M-58-2012, E-7-2011
     * 카테고리: C(건설), E(전기/전자), G(일반), H(보건), M(기계/재료/공정), P(감시/관리), X(보호장비)
     */
    private static final Pattern KOSHA_GUIDE_PATTERN =
            Pattern.compile("^(?<cat>[CEGHMPX])-(?<num>\\d{1,3})-(?<year>\\d{4})$");

    public GeminiService(@Qualifier("geminiRestTemplate") RestTemplate restTemplate,
            GeminiConfig geminiConfig,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * 위험 상황 분석 요청
     * 
     * @param situationText 위험 상황 설명 텍스트
     * @param workType      작업 유형
     * @param location      작업 위치
     * @param workerCount   작업자 수
     * @param currentTask   현재 작업
     * @return 분석 결과 (토큰 사용량 정보 포함)
     */
    public GeminiAnalysisResult analyzeRisk(String situationText, String workType,
            String location, Integer workerCount,
            String currentTask) {
        // 사용자 프롬프트 구성
        String userPrompt = buildUserPrompt(situationText, workType, location, workerCount, currentTask);

        try {
            // 구조화 출력 + 위험성평가 스키마 강제 적용 (Vision 경로와 동일)
            GeminiRequest request = GeminiRequest.builder()
                    .contents(List.of(
                            GeminiRequest.Content.builder()
                                    .role("user")
                                    .parts(List.of(GeminiRequest.Part.fromText(
                                            SYSTEM_PROMPT + "\n\n" + userPrompt)))
                                    .build()))
                    .generationConfig(GeminiRequest.GenerationConfig.builder()
                            .temperature(0.2)
                            .maxOutputTokens(2048)
                            .topP(0.95)
                            .topK(40)
                            .responseMimeType("application/json")
                            .responseSchema(buildRiskAssessmentSchema())
                            .build())
                    .build();

            GeminiResponse response = callGeminiApi(request);
            GeminiAnalysisResult result = parseGeminiResponse(response);

            if (response.getUsageMetadata() != null) {
                GeminiAnalysisResult.UsageMetadata usageMetadata = new GeminiAnalysisResult.UsageMetadata();
                usageMetadata.setPromptTokenCount(response.getUsageMetadata().getPromptTokenCount());
                usageMetadata.setCandidatesTokenCount(response.getUsageMetadata().getCandidatesTokenCount());
                usageMetadata.setTotalTokenCount(response.getUsageMetadata().getTotalTokenCount());
                result.setUsageMetadata(usageMetadata);
            }

            normalizeKoshaGuide(result);
            return result;

        } catch (Exception e) {
            log.error("[Gemini API 오류] 분석 실패, Fallback 응답 반환: {}", e.getMessage());
            return createFallbackResponse(situationText);
        }
    }

    /**
     * 이미지 기반 위험 분석 (Gemini Vision)
     */
    public GeminiAnalysisResult analyzeImage(byte[] imageBytes, String mimeType) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("이미지 데이터가 비어있습니다.");
        }

        String safeMimeType = (mimeType == null || mimeType.isBlank()) ? "image/jpeg" : mimeType;
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(
                        GeminiRequest.Content.builder()
                                .role("user")
                                .parts(List.of(
                                        GeminiRequest.Part.fromText(VISION_PROMPT),
                                        GeminiRequest.Part.fromImage(safeMimeType, base64Image)))
                                .build()))
                .generationConfig(GeminiRequest.GenerationConfig.builder()
                        .temperature(0.2)
                        .maxOutputTokens(2048)
                        .topP(0.95)
                        .topK(40)
                        .responseMimeType("application/json")
                        .responseSchema(buildRiskAssessmentSchema())
                        .build())
                .build();

        try {
            GeminiResponse response = callGeminiApi(request);
            GeminiAnalysisResult result = parseGeminiResponse(response);

            if (response.getUsageMetadata() != null) {
                GeminiAnalysisResult.UsageMetadata usage = new GeminiAnalysisResult.UsageMetadata();
                usage.setPromptTokenCount(response.getUsageMetadata().getPromptTokenCount());
                usage.setCandidatesTokenCount(response.getUsageMetadata().getCandidatesTokenCount());
                usage.setTotalTokenCount(response.getUsageMetadata().getTotalTokenCount());
                result.setUsageMetadata(usage);
            }

            applyExclusionFilter(result);
            normalizeKoshaGuide(result);
            return result;
        } catch (Exception e) {
            log.error("[Gemini Vision 오류] 이미지 분석 실패, Fallback 응답 반환: {}", e.getMessage());
            return createFallbackResponse("사진 기반 위험 상황");
        }
    }

    /**
     * 사용자 프롬프트 생성
     */
    private String buildUserPrompt(String situationText, String workType,
            String location, Integer workerCount,
            String currentTask) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("현장 위험 상황:\n");
        prompt.append(situationText).append("\n\n");

        if (workType != null || location != null || workerCount != null || currentTask != null) {
            prompt.append("추가 정보:\n");
            if (workType != null)
                prompt.append("- 작업 유형: ").append(workType).append("\n");
            if (location != null)
                prompt.append("- 작업 위치: ").append(location).append("\n");
            if (workerCount != null)
                prompt.append("- 작업자 수: ").append(workerCount).append("명\n");
            if (currentTask != null)
                prompt.append("- 현재 작업: ").append(currentTask).append("\n");
        }

        return prompt.toString();
    }

    /**
     * Gemini API 호출
     */
    private GeminiResponse callGeminiApi(GeminiRequest request) {
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        String fullUrl = geminiConfig.getFullUrl();
        String key = geminiConfig.getKey();
        String maskedKey = (key != null && key.length() > 5) ? key.substring(0, 5) + "..." : "null";
        log.info("[Gemini API 요청] URL: {} (Key: {})", fullUrl, maskedKey);

        try {
            String jsonBody = objectMapper.writeValueAsString(request);
            log.debug("[Gemini API 요청] Body: {}", jsonBody);
        } catch (JsonProcessingException e) {
            log.error("[Gemini API 요청] JSON 변환 실패", e);
        }

        try {
            // API 호출
            ResponseEntity<GeminiResponse> responseEntity = restTemplate.exchange(
                    geminiConfig.getFullUrl(),
                    HttpMethod.POST,
                    entity,
                    GeminiResponse.class);

            GeminiResponse response = responseEntity.getBody();

            // 토큰 사용량 로깅
            if (response != null && response.getUsageMetadata() != null) {
                GeminiResponse.UsageMetadata usage = response.getUsageMetadata();
                log.info("[Gemini Usage Log] Input: {}, Output: {}, Total: {}",
                        usage.getPromptTokenCount(),
                        usage.getCandidatesTokenCount(),
                        usage.getTotalTokenCount());
            }

            return response;

        } catch (RestClientException e) {
            // 상세 에러 정보 로깅
            if (e instanceof org.springframework.web.client.HttpStatusCodeException) {
                org.springframework.web.client.HttpStatusCodeException httpError = (org.springframework.web.client.HttpStatusCodeException) e;
                log.error("[Gemini API 호출 실패] StatusCode: {}, ResponseBody: {}",
                        httpError.getStatusCode(),
                        httpError.getResponseBodyAsString());
            } else {
                log.error("[Gemini API 호출 실패] {}", e.getMessage());
            }
            throw new RuntimeException("Gemini API 호출 실패", e);
        }
    }

    /**
     * Gemini 응답에서 분석 결과 파싱
     */
    private GeminiAnalysisResult parseGeminiResponse(GeminiResponse response) {
        if (response == null) {
            throw new RuntimeException("Gemini 응답이 null입니다.");
        }

        String responseText = response.getFirstResponseText();
        if (responseText == null || responseText.isBlank()) {
            throw new RuntimeException("Gemini 응답 텍스트가 비어있습니다.");
        }

        log.debug("[Gemini 응답 텍스트] {}", responseText);

        // JSON 추출 (마크다운 코드 블록 처리)
        String jsonText = extractJson(responseText);

        try {
            return objectMapper.readValue(jsonText, GeminiAnalysisResult.class);
        } catch (JsonProcessingException e) {
            log.error("[Gemini 응답 파싱 실패] JSON: {}, Error: {}", jsonText, e.getMessage());
            throw new RuntimeException("Gemini 응답 파싱 실패", e);
        }
    }

    /**
     * 텍스트에서 JSON 추출
     * - 마크다운 코드 블록 (```json ... ```) 처리
     */
    private String extractJson(String text) {
        // 마크다운 코드 블록에서 JSON 추출
        Pattern pattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 코드 블록이 없으면 { }로 시작하는 JSON 찾기
        Pattern jsonPattern = Pattern.compile("\\{[\\s\\S]*\\}");
        Matcher jsonMatcher = jsonPattern.matcher(text);

        if (jsonMatcher.find()) {
            return jsonMatcher.group(0).trim();
        }

        // JSON을 찾지 못한 경우 원본 반환
        return text.trim();
    }

    /**
     * Gemini 구조화 출력용 JSON Schema 생성.
     * 고용노동부 고시 제2023-19호 위험성평가표 스키마.
     */
    private Map<String, Object> buildRiskAssessmentSchema() {
        Map<String, Object> scoreDetail = new LinkedHashMap<>();
        scoreDetail.put("type", "OBJECT");
        scoreDetail.put("properties", Map.of(
                "score", Map.of("type", "INTEGER"),
                "rationale", Map.of("type", "STRING")));

        Map<String, Object> legalRef = new LinkedHashMap<>();
        legalRef.put("type", "OBJECT");
        legalRef.put("properties", Map.of(
                "law", Map.of("type", "STRING"),
                "article", Map.of("type", "STRING"),
                "content", Map.of("type", "STRING")));

        Map<String, Object> stringArray = Map.of(
                "type", "ARRAY",
                "items", Map.of("type", "STRING"));

        Map<String, Object> controlMeasures = new LinkedHashMap<>();
        controlMeasures.put("type", "OBJECT");
        controlMeasures.put("properties", Map.of(
                "immediate", stringArray,
                "engineering", stringArray,
                "administrative", stringArray,
                "ppe", stringArray));

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("hazardClassification", Map.of("type", "STRING"));
        properties.put("unsafeCondition", Map.of("type", "STRING"));
        properties.put("unsafeAct", Map.of("type", "STRING"));
        properties.put("possibleAccident", Map.of("type", "STRING"));
        properties.put("severity", scoreDetail);
        properties.put("likelihood", scoreDetail);
        properties.put("riskScore", Map.of("type", "INTEGER"));
        properties.put("riskLevel", Map.of("type", "STRING"));
        properties.put("legalBasis", Map.of("type", "ARRAY", "items", legalRef));
        properties.put("koshaGuide", Map.of("type", "STRING"));
        properties.put("controlMeasures", controlMeasures);
        properties.put("responsibleRole", Map.of("type", "STRING"));
        properties.put("dueDays", Map.of("type", "INTEGER"));
        properties.put("confidence", Map.of("type", "NUMBER"));
        properties.put("riskFactor", Map.of("type", "STRING"));
        properties.put("remediationSteps", stringArray);
        properties.put("referenceCode", Map.of("type", "STRING"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "OBJECT");
        schema.put("properties", properties);
        schema.put("required", List.of("riskFactor", "riskLevel", "remediationSteps"));
        return schema;
    }

    /**
     * Fallback 응답 생성 (Gemini API 실패 시).
     *
     * - AI가 응답하지 못했음을 사용자에게 명확히 표기(riskFactor에 [AI 분석 불가] 접두어)
     * - 실제 KOSHA Guide 코드 사용 (정식 포맷: {카테고리}-{번호}-{연도})
     * - 빈도×강도 보수적 추정값 제공 (조작 방지를 위해 명시적으로 "AI 미추정"으로 rationale 표기)
     * - 통제 위계 4단계 구조화
     */
    private GeminiAnalysisResult createFallbackResponse(String situationText) {
        String lowerText = situationText != null ? situationText.toLowerCase() : "";
        GeminiAnalysisResult fallback = new GeminiAnalysisResult();
        fallback.setConfidence(0.0);
        fallback.setResponsibleRole("안전관리자");
        fallback.setDueDays(1);

        GeminiAnalysisResult.ControlMeasures cm = new GeminiAnalysisResult.ControlMeasures();

        if (lowerText.contains("안전대") || lowerText.contains("추락") || lowerText.contains("고소")
                || lowerText.contains("비계") || lowerText.contains("개구부")) {
            fallback.setRiskFactor("[AI 분석 불가] 고소 작업 추락 위험(키워드 기반 임시 분류)");
            fallback.setRiskLevel("HIGH");
            fallback.setKoshaGuide("G-99-2012");
            fallback.setReferenceCode("G-99-2012");
            fallback.setHazardClassification("작업특성");
            fallback.setUnsafeCondition("추락 방호조치(안전난간/개구부 덮개/안전대 부착설비) 미흡 가능성");
            fallback.setPossibleAccident("추락");
            fallback.setSeverity(scoreDetail(4, "AI 미추정 — 2m 이상 추락은 사망 가능"));
            fallback.setLikelihood(scoreDetail(3, "AI 미추정 — 보수적 추정"));
            fallback.setRiskScore(12);
            fallback.setLegalBasis(List.of(
                    legalRef("산업안전보건기준에 관한 규칙", "제42조", "추락의 방지 — 2m 이상 작업장소 안전난간 등 설치"),
                    legalRef("산업안전보건기준에 관한 규칙", "제43조", "개구부 등의 방호 조치")));
            cm.setImmediate(List.of("작업 즉시 중단", "해당 구역 출입 통제"));
            cm.setEngineering(List.of("안전난간(1.0m 이상) 설치", "개구부 덮개 또는 안전망 설치"));
            cm.setAdministrative(List.of("작업허가서 발급", "일일 TBM에서 위치 공유"));
            cm.setPpe(List.of("안전대 체결 후 생명줄 연결", "안전모"));
            fallback.setRemediationSteps(List.of(
                    "즉시 작업을 중단하고 해당 구역을 출입 통제하십시오.",
                    "안전난간·개구부 덮개를 설치하십시오.",
                    "안전대 부착설비에 연결한 뒤 작업을 재개하십시오."));
        } else if (lowerText.contains("화기") || lowerText.contains("용접") || lowerText.contains("가연")
                || lowerText.contains("화재") || lowerText.contains("불꽃")) {
            fallback.setRiskFactor("[AI 분석 불가] 화기 작업 화재 위험(키워드 기반 임시 분류)");
            fallback.setRiskLevel("CRITICAL");
            fallback.setKoshaGuide("M-58-2012");
            fallback.setReferenceCode("M-58-2012");
            fallback.setHazardClassification("화학적");
            fallback.setUnsafeCondition("화기작업 반경 내 가연물 존재 / 화재감시자 미배치 가능성");
            fallback.setPossibleAccident("화재");
            fallback.setSeverity(scoreDetail(4, "AI 미추정 — 화재 확산 시 인명/설비 피해 중대"));
            fallback.setLikelihood(scoreDetail(4, "AI 미추정 — 착화원 상시 존재"));
            fallback.setRiskScore(16);
            fallback.setLegalBasis(List.of(
                    legalRef("산업안전보건기준에 관한 규칙", "제241조", "화재위험작업 시의 준수사항"),
                    legalRef("산업안전보건기준에 관한 규칙", "제236조", "화재감시자 배치")));
            cm.setImmediate(List.of("용접 즉시 중단", "반경 10m 내 가연물 제거 또는 방염포 피복"));
            cm.setEngineering(List.of("불꽃 비산 방지막 설치", "소화기 2대 이상 비치"));
            cm.setAdministrative(List.of("화기작업 허가서 발급", "화재감시자 1인 배치"));
            cm.setPpe(List.of("용접면", "내열 장갑", "안전화"));
            fallback.setRemediationSteps(List.of(
                    "용접을 즉시 중단하고 반경 10m 내 가연물을 제거하십시오.",
                    "화기작업 허가서를 발급받고 화재감시자를 배치하십시오.",
                    "소화기를 옆에 비치한 뒤 작업을 재개하십시오."));
        } else if (lowerText.contains("밀폐") || lowerText.contains("산소") || lowerText.contains("질식")
                || lowerText.contains("탱크") || lowerText.contains("맨홀")) {
            fallback.setRiskFactor("[AI 분석 불가] 밀폐공간 질식 위험(키워드 기반 임시 분류)");
            fallback.setRiskLevel("CRITICAL");
            fallback.setKoshaGuide("H-80-2012");
            fallback.setReferenceCode("H-80-2012");
            fallback.setHazardClassification("화학적");
            fallback.setUnsafeCondition("밀폐공간 산소농도 미확인 / 환기설비 미가동 가능성");
            fallback.setPossibleAccident("질식");
            fallback.setSeverity(scoreDetail(4, "AI 미추정 — 질식은 수분 내 사망 가능"));
            fallback.setLikelihood(scoreDetail(3, "AI 미추정"));
            fallback.setRiskScore(12);
            fallback.setLegalBasis(List.of(
                    legalRef("산업안전보건기준에 관한 규칙", "제619조", "밀폐공간 작업 프로그램의 수립·시행"),
                    legalRef("산업안전보건기준에 관한 규칙", "제622조", "산소·유해가스 농도 측정")));
            cm.setImmediate(List.of("밀폐공간 진입 즉시 금지"));
            cm.setEngineering(List.of("환기설비 가동", "산소농도 측정기 18% 이상 확인"));
            cm.setAdministrative(List.of("밀폐공간 작업허가서 발급", "감시인 상주"));
            cm.setPpe(List.of("송기마스크 또는 공기호흡기"));
            fallback.setRemediationSteps(List.of(
                    "밀폐공간 진입을 즉시 금지하십시오.",
                    "산소농도를 측정하고(18% 이상) 환기설비를 가동하십시오.",
                    "작업허가서 발급과 감시인 배치 후 진입하십시오."));
        } else if (lowerText.contains("전기") || lowerText.contains("감전") || lowerText.contains("누전")) {
            fallback.setRiskFactor("[AI 분석 불가] 전기작업 감전 위험(키워드 기반 임시 분류)");
            fallback.setRiskLevel("HIGH");
            fallback.setKoshaGuide("E-68-2012");
            fallback.setReferenceCode("E-68-2012");
            fallback.setHazardClassification("전기적");
            fallback.setPossibleAccident("감전");
            fallback.setSeverity(scoreDetail(4, "AI 미추정 — 감전은 사망 가능"));
            fallback.setLikelihood(scoreDetail(3, "AI 미추정"));
            fallback.setRiskScore(12);
            fallback.setLegalBasis(List.of(
                    legalRef("산업안전보건기준에 관한 규칙", "제319조", "정전 전기작업"),
                    legalRef("산업안전보건기준에 관한 규칙", "제302조", "전기 기계·기구의 접지")));
            cm.setImmediate(List.of("전원 차단(LOTO)", "검전기로 무전압 확인"));
            cm.setEngineering(List.of("접지선 설치", "누전차단기 작동 확인"));
            cm.setAdministrative(List.of("정전작업 허가서 발급", "2인 1조 작업"));
            cm.setPpe(List.of("절연장갑", "절연화", "안전모"));
            fallback.setRemediationSteps(List.of(
                    "전원을 차단하고 LOTO(Lockout/Tagout)를 시행하십시오.",
                    "검전기로 무전압을 확인하고 접지선을 설치하십시오.",
                    "절연장갑 착용 후 2인 1조로 작업하십시오."));
        } else {
            fallback.setRiskFactor("[AI 분석 불가] 현장 일반 안전 위험(키워드 매칭 실패)");
            fallback.setRiskLevel("MEDIUM");
            fallback.setKoshaGuide("G-140-2024");
            fallback.setReferenceCode("G-140-2024");
            fallback.setHazardClassification("작업특성");
            fallback.setUnsafeCondition("구체적 위험요인 식별을 위해 재분석 필요");
            fallback.setSeverity(scoreDetail(2, "AI 미추정 — 보수적 추정"));
            fallback.setLikelihood(scoreDetail(3, "AI 미추정"));
            fallback.setRiskScore(6);
            fallback.setLegalBasis(List.of(
                    legalRef("산업안전보건법", "제36조", "위험성평가 실시"),
                    legalRef("산업안전보건법", "제38조", "안전조치")));
            cm.setImmediate(List.of("작업 일시 중단", "현장 안전상태 점검"));
            cm.setAdministrative(List.of("안전관리자 확인", "위험성평가 재실시"));
            cm.setPpe(List.of("기본 개인보호구(안전모, 안전화) 착용 확인"));
            fallback.setRemediationSteps(List.of(
                    "[AI 분석 실패] 안전관리자에게 상세 보고 후 위험성평가를 재실시하십시오.",
                    "작업자 보호구 착용 상태를 전수 점검하십시오.",
                    "작업 재개 전 관리감독자 확인을 받으십시오."));
        }

        fallback.setControlMeasures(cm);
        return fallback;
    }

    private GeminiAnalysisResult.ScoreDetail scoreDetail(int score, String rationale) {
        GeminiAnalysisResult.ScoreDetail d = new GeminiAnalysisResult.ScoreDetail();
        d.setScore(score);
        d.setRationale(rationale);
        return d;
    }

    private GeminiAnalysisResult.LegalReference legalRef(String law, String article, String content) {
        GeminiAnalysisResult.LegalReference r = new GeminiAnalysisResult.LegalReference();
        r.setLaw(law);
        r.setArticle(article);
        r.setContent(content);
        return r;
    }

    /**
     * KOSHA Guide 코드 정규화.
     * - 유효한 포맷({카테고리}-{번호}-{연도})이 아니면 koshaGuide/referenceCode 중
     *   패턴에 맞는 값으로 대체하고, 둘 다 무효면 null로 정리한다.
     * - 공백/접두어("KOSHA-", "KOSHA Guide ") 제거, 대문자 카테고리로 통일.
     */
    private void normalizeKoshaGuide(GeminiAnalysisResult result) {
        if (result == null) {
            return;
        }

        String kosha = cleanKoshaCandidate(result.getKoshaGuide());
        String ref = cleanKoshaCandidate(result.getReferenceCode());

        String validKosha = matchesKoshaPattern(kosha) ? kosha : null;
        String validRef = matchesKoshaPattern(ref) ? ref : null;

        if (validKosha == null && validRef != null) {
            validKosha = validRef;
        }
        if (validRef == null && validKosha != null) {
            validRef = validKosha;
        }

        result.setKoshaGuide(validKosha);
        result.setReferenceCode(validRef);
    }

    private String cleanKoshaCandidate(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        // 흔한 접두어 제거
        s = s.replaceFirst("(?i)^kosha[\\s-]+guide[\\s-]+", "");
        s = s.replaceFirst("(?i)^kosha[\\s-]+", "");
        s = s.replace(" ", "").toUpperCase();
        return s;
    }

    private boolean matchesKoshaPattern(String s) {
        if (s == null) {
            return false;
        }
        Matcher m = KOSHA_GUIDE_PATTERN.matcher(s);
        return m.matches();
    }

    /**
     * 분석 제외 대상 키워드가 응답에 포함되면 일반 안전 권고로 정규화
     */
    private void applyExclusionFilter(GeminiAnalysisResult result) {
        if (result == null) {
            return;
        }

        String mergedText = String.join(" ",
                result.getRiskFactor() != null ? result.getRiskFactor().toLowerCase() : "",
                result.getReferenceCode() != null ? result.getReferenceCode().toLowerCase() : "",
                result.getRemediationSteps() != null ? String.join(" ", result.getRemediationSteps()).toLowerCase() : "");

        boolean containsExcluded = EXCLUDED_KEYWORDS.stream()
                .map(String::toLowerCase)
                .anyMatch(mergedText::contains);

        if (containsExcluded) {
            log.info("[Gemini Vision] 제외 대상 키워드 감지 - 응답 정규화");
            result.setRiskFactor("제외 대상 요소를 제외하고 일반 안전 위험 요소를 재분석했습니다.");
            result.setRiskLevel("MEDIUM");
            result.setReferenceCode("KOSHA-P-2023-12");
            result.setRemediationSteps(List.of(
                    "작업 구역 주변의 기본 안전수칙 준수 여부를 점검하십시오.",
                    "개인보호구 착용 상태를 확인하십시오.",
                    "현장 안전관리자에게 추가 점검을 요청하십시오."
            ));
        }
    }
}
