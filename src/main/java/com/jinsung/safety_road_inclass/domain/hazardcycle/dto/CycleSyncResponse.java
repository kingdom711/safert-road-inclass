package com.jinsung.safety_road_inclass.domain.hazardcycle.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleSyncResponse {

    private int syncedCount;
    private int failedCount;
    private List<SyncResult> results;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResult {
        private String clientTempId;
        private Long serverId;
        private String status;
        private HazardCycleResponse.AiAnalysisDetail aiAnalysis;
        private HazardCycleResponse.RewardDetail tier1Reward;
        private String error;
    }
}
