package com.jinsung.safety_road_inclass.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GEMS AI 분석 응답 DTO
 * - 프론트엔드 가이드 형식에 맞춤
 */
@Getter
@Builder
public class BusinessPlanResponse {
    
    /**
     * 식별된 위험 요인 (단수형)
     */
    private String riskFactor;
    
    /**
     * 조치 단계 목록
     */
    private List<String> remediationSteps;
    
    /**
     * KOSHA 참조 코드
     */
    private String referenceCode;
    
    /**
     * 조치 기록 ID
     */
    private String actionRecordId;
    
    /**
     * 위험 수준: LOW, MEDIUM, HIGH, CRITICAL
     */
    private String riskLevel;
    
    /**
     * 분석 ID
     */
    private String analysisId;
    
    /**
     * 분석 일시
     */
    private LocalDateTime analyzedAt;
}

