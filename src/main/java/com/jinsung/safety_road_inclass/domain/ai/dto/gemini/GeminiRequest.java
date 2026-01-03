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
    public static class Content {
        private String role;
        private List<Part> parts;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig {
        private Double temperature;
        private Integer maxOutputTokens;
        private Double topP;
        private Integer topK;
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
                            Part.builder().text(systemPrompt + "\n\n" + userPrompt).build()
                        ))
                        .build()
                ))
                .generationConfig(GenerationConfig.builder()
                    .temperature(0.7)
                    .maxOutputTokens(1024)
                    .topP(0.95)
                    .topK(40)
                    .build())
                .build();
    }
}

