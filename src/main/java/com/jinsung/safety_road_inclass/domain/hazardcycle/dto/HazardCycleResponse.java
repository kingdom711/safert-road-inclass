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
