package com.jinsung.safety_road_inclass.domain.ai.dto.gemini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Gemini API 요청 DTO
 * - Google Generative AI API 스펙에 맞춘 요청 구조
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class GeminiRequest {

    /**
     * 요청 컨텐츠 목록
     */
    private List<Content> contents;

    /**
     * 생성 설정 (Optional)
     */
    private GenerationConfig generationConfig;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public static class Content {
        private String role;
        private List<Part> parts;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public static class Part {
        private String text;
        private InlineData inlineData;

        public static Part fromText(String text) {
            return Part.builder()
                    .text(text)
                    .build();
        }

        public static Part fromImage(String mimeType, String base64Data) {
            return Part.builder()
                    .inlineData(InlineData.builder()
                            .mimeType(mimeType)
                            .data(base64Data)
                            .build())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public static class InlineData {
        private String mimeType;
        private String data;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public static class GenerationConfig {
        private Double temperature;
        private Integer maxOutputTokens;
        private Double topP;
        private Integer topK;

        /** Gemini 구조화 출력: "application/json" 지정 시 JSON 준수율 향상 */
        private String responseMimeType;

        /** JSON Schema 객체 (optional) — 스키마 강제로 파싱 실패율 감소 */
        private java.util.Map<String, Object> responseSchema;
    }

    /**
     * 간단한 텍스트 프롬프트로 요청 생성
     */
    public static GeminiRequest fromPrompt(String systemPrompt, String userPrompt) {
        return GeminiRequest.builder()
                .contents(List.of(
                        Content.builder()
                                .role("user")
                                .parts(List.of(
                                        Part.fromText(systemPrompt + "\n\n" + userPrompt)))
                                .build()))
                .generationConfig(GenerationConfig.builder()
                        .temperature(0.7)
                        .maxOutputTokens(1024)
                        .topP(0.95)
                        .topK(40)
                        .build())
                .build();
    }
}
