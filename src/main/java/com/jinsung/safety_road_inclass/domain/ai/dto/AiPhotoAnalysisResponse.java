package com.jinsung.safety_road_inclass.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 이미지 분석 응답 DTO
 */
@Getter
@Builder
public class AiPhotoAnalysisResponse {
    
    private String riskLevel;
    private List<String> detectedIssues;
    private List<String> tags;
    private String analysisSource;
    private LocalDateTime analyzedAt;
    private String message;
}


