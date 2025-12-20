package com.jinsung.safety_road_inclass.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 분석 응답 DTO
 */
@Getter
@Builder
@Schema(description = "AI 분석 응답")
public class AiAnalysisResponse {
    
    @Schema(description = "위험 등급", example = "HIGH")
    private String riskLevel;
    
    @Schema(description = "위험 요인 목록")
    private List<String> riskFactors;
    
    @Schema(description = "개선 권장사항 목록")
    private List<String> recommendations;
    
    @Schema(description = "분석 소스", example = "Gemini API (KOSHA-G-2023-01)")
    private String analysisSource;
    
    @Schema(description = "분석 일시")
    private LocalDateTime analyzedAt;
    
    @Schema(description = "분석 완료 메시지")
    private String message;
    
    @Schema(description = "Gemini API 토큰 사용량 정보")
    private Usage usage;
    
    /**
     * Gemini API 토큰 사용량 정보
     */
    @Getter
    @Builder
    @Schema(description = "토큰 사용량 정보")
    public static class Usage {
        @Schema(description = "프롬프트 토큰 수 (입력)", example = "150")
        private Integer promptTokens;
        
        @Schema(description = "후보 토큰 수 (출력)", example = "200")
        private Integer candidatesTokens;
        
        @Schema(description = "총 토큰 수", example = "350")
        private Integer totalTokens;
    }
}


