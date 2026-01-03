package com.jinsung.safety_road_inclass.domain.ai.dto.gemini;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Gemini API 응답 DTO
 * - Google Generative AI API 응답 구조
 */
@Getter
@Setter
@NoArgsConstructor
public class GeminiResponse {
    
    /**
     * 후보 응답 목록
     */
    private List<Candidate> candidates;
    
    /**
     * 토큰 사용량 메타데이터
     */
    private UsageMetadata usageMetadata;
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Candidate {
        private Content content;
        private String finishReason;
        private Integer index;
        private List<SafetyRating> safetyRatings;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
        private String role;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Part {
        private String text;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class SafetyRating {
        private String category;
        private String probability;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UsageMetadata {
        private Integer promptTokenCount;
        private Integer candidatesTokenCount;
        private Integer totalTokenCount;
    }
    
    /**
     * 첫 번째 응답 텍스트 추출
     */
    public String getFirstResponseText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.get(0);
            if (firstCandidate.getContent() != null 
                && firstCandidate.getContent().getParts() != null 
                && !firstCandidate.getContent().getParts().isEmpty()) {
                return firstCandidate.getContent().getParts().get(0).getText();
            }
        }
        return null;
    }
}

