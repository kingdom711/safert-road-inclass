package com.jinsung.safety_road_inclass.domain.hazardcycle.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleStatsResponse {

    private long totalCycles;
    private long completedCycles;
    private long pendingCycles;
    private double completionRate;
    private int totalPointsEarned;
    private long thisWeekCycles;
    private Map<String, Long> riskLevelDistribution;
}
