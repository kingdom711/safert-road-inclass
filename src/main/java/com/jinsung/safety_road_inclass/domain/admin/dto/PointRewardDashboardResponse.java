package com.jinsung.safety_road_inclass.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PointRewardDashboardResponse {

    private final LocalDate from;
    private final LocalDate to;
    private final Summary summary;
    private final List<UserBalanceRow> users;
    private final List<RewardDemandRow> pendingRewards;

    @Getter
    @Builder
    public static class Summary {
        private final int totalParticipants;
        private final long totalPointBalance;
        private final long periodPointsEarned;
        private final long periodPointsSpent;
        private final long totalGoldBalance;
        private final long periodGoldEarned;
        private final long periodGoldSpent;
        private final long pendingRewardRequests;
        private final long pendingRewardGold;
        private final long pendingRewardCashValue;
    }

    @Getter
    @Builder
    public static class UserBalanceRow {
        private final Long userId;
        private final String username;
        private final String name;
        private final Long teamId;
        private final String teamName;
        private final int pointBalance;
        private final int totalPointsEarned;
        private final int totalPointsSpent;
        private final long periodPointsEarned;
        private final long periodPointsSpent;
        private final int goldBalance;
        private final int totalGoldEarned;
        private final int totalGoldSpent;
        private final long periodGoldEarned;
        private final long periodGoldSpent;
        private final long pendingRewardRequests;
        private final long pendingRewardGold;
        private final LocalDateTime lastPointActivityAt;
    }

    @Getter
    @Builder
    public static class RewardDemandRow {
        private final Long rewardId;
        private final String rewardName;
        private final String rewardType;
        private final long pendingCount;
        private final long requiredGold;
        private final long cashValue;
        private final int remainingQuantity;
    }
}
