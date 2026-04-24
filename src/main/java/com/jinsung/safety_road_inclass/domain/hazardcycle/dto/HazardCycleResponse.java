package com.jinsung.safety_road_inclass.domain.hazardcycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Hazard Cycle 응답")
public class HazardCycleResponse {

    private Long id;
    private String status;
    private String hazardPhotoUrl;
    private String hazardDescription;
    private String locationDescription;
    private LocalDateTime reportedAt;
    private AiAnalysisDetail aiAnalysis;
    private String completionPhotoUrl;
    private String completionNote;
    private LocalDateTime completedAt;
    private RewardDetail tier1Reward;
    private RewardDetail tier2Reward;
    private int totalPointsEarned;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiAnalysisDetail {
        private String riskLevel;
        private String riskFactor;
        private List<String> remediationSteps;
        private String referenceCode;
        private LocalDateTime analyzedAt;

        // 확장 위험성평가 필드
        private String hazardClassification;
        private String unsafeCondition;
        private String unsafeAct;
        private String possibleAccident;
        private ScoreDetail severity;
        private ScoreDetail likelihood;
        private Integer riskScore;
        private List<LegalReference> legalBasis;
        private String koshaGuide;
        private ControlMeasures controlMeasures;
        private String responsibleRole;
        private Integer dueDays;
        private Double confidence;

        // 캐시 재사용 출처 (null이면 신규 분석)
        private Long cacheSourceId;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreDetail {
        private Integer score;
        private String rationale;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LegalReference {
        private String law;
        private String article;
        private String content;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ControlMeasures {
        private List<String> immediate;
        private List<String> engineering;
        private List<String> administrative;
        private List<String> ppe;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardDetail {
        private int pointsAwarded;
        private int newBalance;
        private String message;
    }
}
