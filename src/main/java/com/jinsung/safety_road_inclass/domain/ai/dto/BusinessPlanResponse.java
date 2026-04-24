package com.jinsung.safety_road_inclass.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GEMS AI 분석 응답 DTO.
 *
 * 고용노동부 고시 제2023-19호(사업장 위험성평가에 관한 지침) 준수.
 * - Legacy 필드(riskFactor/remediationSteps/referenceCode/riskLevel)는 호환성을 위해 유지
 * - 확장 필드로 빈도×강도, 법적 근거, 통제 위계, KRAS 분류 추가
 */
@Getter
@Builder
@Schema(description = "GEMS AI 분석 응답")
public class BusinessPlanResponse {

    // Legacy / summary
    @Schema(description = "식별된 위험 요인")
    private String riskFactor;

    @Schema(description = "조치 단계 목록")
    private List<String> remediationSteps;

    @Schema(description = "KOSHA Guide 코드 (정식 포맷)")
    private String referenceCode;

    @Schema(description = "위험 수준", allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private String riskLevel;

    // 분석 메타
    @Schema(description = "조치 기록 ID")
    private String actionRecordId;

    @Schema(description = "분석 ID")
    private String analysisId;

    @Schema(description = "분석 일시")
    private LocalDateTime analyzedAt;

    // 확장 위험성평가 필드 (고시 제2023-19호)
    @Schema(description = "KRAS 유해위험요인 분류")
    private String hazardClassification;

    @Schema(description = "불안전 상태")
    private String unsafeCondition;

    @Schema(description = "불안전 행동")
    private String unsafeAct;

    @Schema(description = "예상 재해 유형")
    private String possibleAccident;

    private ScoreDetail severity;
    private ScoreDetail likelihood;

    @Schema(description = "위험성 R = L × S")
    private Integer riskScore;

    @Schema(description = "법적 근거")
    private List<LegalReference> legalBasis;

    @Schema(description = "KOSHA Guide 정식 코드")
    private String koshaGuide;

    private ControlMeasures controlMeasures;

    @Schema(description = "조치 책임자")
    private String responsibleRole;

    @Schema(description = "조치 권장 기한(일)")
    private Integer dueDays;

    @Schema(description = "AI 신뢰도 (0.0~1.0)")
    private Double confidence;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreDetail {
        private Integer score;
        private String rationale;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LegalReference {
        private String law;
        private String article;
        private String content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ControlMeasures {
        private List<String> immediate;
        private List<String> engineering;
        private List<String> administrative;
        private List<String> ppe;
    }
}
