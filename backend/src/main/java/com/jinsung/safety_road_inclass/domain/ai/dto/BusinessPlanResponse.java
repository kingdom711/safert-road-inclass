package com.jinsung.safety_road_inclass.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "GEMS AI 분석 응답")
public class BusinessPlanResponse {
    
    /**
     * 식별된 위험 요인 (단수형)
     */
    @Schema(description = "식별된 위험 요인", example = "고소 작업 중 안전대 미체결")
    private String riskFactor;
    
    /**
     * 조치 단계 목록
     */
    @Schema(description = "조치 단계 목록", example = "[\"즉시 작업을 중단하고 안전한 장소로 이동하십시오.\", \"안전대 및 부속품의 상태를 점검하십시오.\"]")
    private List<String> remediationSteps;
    
    /**
     * KOSHA 참조 코드
     */
    @Schema(description = "KOSHA 참조 코드", example = "KOSHA-G-2023-01")
    private String referenceCode;
    
    /**
     * 조치 기록 ID
     */
    @Schema(description = "조치 기록 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String actionRecordId;
    
    /**
     * 위험 수준: LOW, MEDIUM, HIGH, CRITICAL
     */
    @Schema(description = "위험 수준", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private String riskLevel;
    
    /**
     * 분석 ID
     */
    @Schema(description = "분석 ID", example = "analysis-2024-12-17-001")
    private String analysisId;
    
    /**
     * 분석 일시
     */
    @Schema(description = "분석 일시", example = "2024-12-17T10:30:00.000Z")
    private LocalDateTime analyzedAt;
}

