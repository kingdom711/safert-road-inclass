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
import java.util.List;
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
     * 산업안전보건 전문가 시스템 프롬프트
     */
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

            반드시 위 4가지 필드만 JSON 형식으로 응답하세요. 다른 텍스트 없이 순수 JSON만 반환하세요.
            """;

    private static final String VISION_PROMPT = """
            당신은 건설/산업 현장 안전 전문가입니다. 제공된 사진을 분석하여 위험 요소를 식별하세요.

            배관 구경에 따른 질식 위험 및 훅업(Hook-up) 작업 관련 요소는 분석 대상에서 제외합니다.

            [응답 형식 - 반드시 JSON으로 응답]
            {
                "riskFactor": "핵심 위험 요소 설명",
                "riskLevel": "CRITICAL|HIGH|MEDIUM|LOW",
                "remediationSteps": ["조치1", "조치2", "조치3"],
                "referenceCode": "KOSHA-X-XXXX-XX"
            }

            [KOSHA 참조 코드]
            - KOSHA-G-2023-01: 고소작업 + 안전대
            - KOSHA-M-2023-05: 화기작업 + 화재예방
            - KOSHA-P-2023-12: 개인보호구
            - KOSHA-C-2023-08: 가설구조물
            - KOSHA-S-2023-03: 밀폐공간
            - KOSHA-E-2023-07: 전기안전
            - KOSHA-L-2023-11: 중량물/크레인
            - KOSHA-F-2023-04: 화재예방/용접
            """;

    private static final Set<String> EXCLUDED_KEYWORDS = Set.of("훅업", "hook-up", "배관 구경");

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
            // Gemini API 호출
            GeminiResponse response = callGeminiApi(userPrompt);

            // 응답 파싱 (토큰 정보 포함)
            GeminiAnalysisResult result = parseGeminiResponse(response);

            // 토큰 사용량 정보 추가
            if (response.getUsageMetadata() != null) {
                GeminiAnalysisResult.UsageMetadata usageMetadata = new GeminiAnalysisResult.UsageMetadata();
                usageMetadata.setPromptTokenCount(response.getUsageMetadata().getPromptTokenCount());
                usageMetadata.setCandidatesTokenCount(response.getUsageMetadata().getCandidatesTokenCount());
                usageMetadata.setTotalTokenCount(response.getUsageMetadata().getTotalTokenCount());
                result.setUsageMetadata(usageMetadata);
            }

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
                        .maxOutputTokens(1024)
                        .topP(0.95)
                        .topK(40)
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
    private GeminiResponse callGeminiApi(String userPrompt) {
        // 요청 생성
        GeminiRequest request = GeminiRequest.fromPrompt(SYSTEM_PROMPT, userPrompt);
        return callGeminiApi(request);
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
     * Fallback 응답 생성 (API 실패 시)
     */
    private GeminiAnalysisResult createFallbackResponse(String situationText) {
        GeminiAnalysisResult fallback = new GeminiAnalysisResult();

        // 입력 텍스트 기반 기본 분석
        String lowerText = situationText != null ? situationText.toLowerCase() : "";

        if (lowerText.contains("안전대") || lowerText.contains("추락") || lowerText.contains("고소")) {
            fallback.setRiskFactor("고소 작업 중 추락 위험 감지");
            fallback.setRiskLevel("HIGH");
            fallback.setReferenceCode("KOSHA-G-2023-01");
            fallback.setRemediationSteps(List.of(
                    "즉시 작업을 중단하고 안전한 장소로 이동하십시오.",
                    "안전대 및 부속품의 상태를 점검하십시오.",
                    "안전대 체결 후 2인 1조로 작업을 재개하십시오."));
        } else if (lowerText.contains("화기") || lowerText.contains("용접") || lowerText.contains("가연")
                || lowerText.contains("화재")) {
            fallback.setRiskFactor("화기 작업 중 화재 위험 감지");
            fallback.setRiskLevel("CRITICAL");
            fallback.setReferenceCode("KOSHA-M-2023-05");
            fallback.setRemediationSteps(List.of(
                    "반경 10m 이내 가연성 물질을 제거하거나 방염포로 덮으십시오.",
                    "소화기를 작업 장소 바로 옆에 비치하십시오.",
                    "화기 감시자를 배치하고 작업을 진행하십시오."));
        } else if (lowerText.contains("밀폐") || lowerText.contains("산소") || lowerText.contains("질식")) {
            fallback.setRiskFactor("밀폐공간 작업 중 질식 위험 감지");
            fallback.setRiskLevel("CRITICAL");
            fallback.setReferenceCode("KOSHA-S-2023-03");
            fallback.setRemediationSteps(List.of(
                    "밀폐공간 진입을 즉시 금지하십시오.",
                    "산소 농도 측정기로 농도를 확인하십시오 (18% 이상 필요).",
                    "환기 장치를 가동하고 충분히 환기하십시오.",
                    "밀폐공간 작업 허가서를 발급받은 후 진입하십시오."));
        } else {
            fallback.setRiskFactor("현장 안전 위험 요소 감지");
            fallback.setRiskLevel("MEDIUM");
            fallback.setReferenceCode("KOSHA-P-2023-12");
            fallback.setRemediationSteps(List.of(
                    "해당 구역의 작업을 즉시 중단하십시오.",
                    "현장 안전 상태를 점검하십시오.",
                    "작업자 전원 안전모 및 보호구 착용을 확인하십시오.",
                    "작업 재개 전 안전관리자의 확인을 받으십시오."));
        }

        return fallback;
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
