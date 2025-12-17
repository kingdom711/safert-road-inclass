package com.jinsung.safety_road_inclass.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 분석 응답 DTO
 */
@Getter
@Builder
public class AiAnalysisResponse {
    
    private String riskLevel;
    private List<String> riskFactors;
    private List<String> recommendations;
    private String analysisSource;
    private LocalDateTime analyzedAt;
    private String message;
}

