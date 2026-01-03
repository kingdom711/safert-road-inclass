package com.jinsung.safety_road_inclass.domain.ai.dto.gemini;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Gemini AI가 반환하는 위험 분석 결과 DTO
 * - Gemini 응답 JSON을 파싱한 구조화된 결과
 */
@Getter
@Setter
@NoArgsConstructor
public class GeminiAnalysisResult {
    
    /**
     * 핵심 위험 요인 (한 문장)
     */
    private String riskFactor;
    
    /**
     * 위험 등급: CRITICAL, HIGH, MEDIUM, LOW
     */
    private String riskLevel;
    
    /**
     * 구체적인 조치 방안 (3~5개)
     */
    private List<String> remediationSteps;
    
    /**
     * KOSHA 가이드 참조 코드
     */
    private String referenceCode;
    
    /**
     * 토큰 사용량 정보
     */
    private UsageMetadata usageMetadata;
    
    /**
     * 토큰 사용량 메타데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UsageMetadata {
        private Integer promptTokenCount;
        private Integer candidatesTokenCount;
        private Integer totalTokenCount;
    }
}

