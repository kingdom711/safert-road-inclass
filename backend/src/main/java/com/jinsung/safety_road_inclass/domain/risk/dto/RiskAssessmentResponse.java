package com.jinsung.safety_road_inclass.domain.risk.dto;

import com.jinsung.safety_road_inclass.domain.risk.entity.RiskAssessment;
import com.jinsung.safety_road_inclass.domain.risk.entity.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RiskAssessmentResponse - 위험성 평가 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "위험성 평가 응답")
public class RiskAssessmentResponse {

    @Schema(description = "평가 ID", example = "1")
    private Long id;

    @Schema(description = "체크리스트 항목 ID", example = "5")
    private Long checklistItemId;

    @Schema(description = "체크리스트 ID", example = "1")
    private Long checklistId;

    @Schema(description = "항목 내용", example = "안전모를 착용하였는가?")
    private String itemContent;

    @Schema(description = "빈도", example = "3")
    private Integer frequency;

    @Schema(description = "강도", example = "4")
    private Integer severity;

    @Schema(description = "위험도 점수", example = "12")
    private Integer riskScore;

    @Schema(description = "위험 레벨", example = "HIGH")
    private RiskLevel riskLevel;

    @Schema(description = "설명", example = "추락 위험이 높은 작업 환경")
    private String description;

    @Schema(description = "평가자 이름", example = "김관리")
    private String assessedByName;

    @Schema(description = "평가 시간", example = "2025-12-06T10:30:00")
    private LocalDateTime assessedAt;

    @Schema(description = "개선 대책 목록")
    private List<CountermeasureResponse> countermeasures;

    public static RiskAssessmentResponse from(RiskAssessment assessment) {
        return RiskAssessmentResponse.builder()
                .id(assessment.getId())
                .checklistItemId(assessment.getChecklistItem().getId())
                .checklistId(assessment.getChecklistItem().getChecklist().getId())
                .itemContent(assessment.getChecklistItem().getTemplateItem().getContent())
                .frequency(assessment.getFrequency())
                .severity(assessment.getSeverity())
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel())
                .description(assessment.getDescription())
                .assessedByName(assessment.getAssessedBy().getName())
                .assessedAt(assessment.getAssessedAt())
                .countermeasures(assessment.getCountermeasures().stream()
                        .map(CountermeasureResponse::from)
                        .toList())
                .build();
    }
}

