package com.jinsung.safety_road_inclass.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AdminDashboardSummaryResponse {

    private final LocalDateTime generatedAt;
    private final Metrics metrics;
    private final List<ActionItem> actionItems;
    private final List<RecentActivity> recentActivities;
    private final Map<String, Long> workStopByHazardType;

    @Getter
    @Builder
    public static class Metrics {
        private final long totalUsers;
        private final long totalTeams;
        private final long todayEducationCompletions;
        private final long todayChecklistSubmissions;
        private final long todayHazardReports;
        private final long openWorkStopReports;
        private final long pendingRewardRequests;
        private final long openHazardCycles;
    }

    @Getter
    @Builder
    public static class ActionItem {
        private final String type;
        private final String title;
        private final String detail;
        private final String status;
        private final String href;
        private final LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class RecentActivity {
        private final String type;
        private final String title;
        private final String detail;
        private final LocalDateTime occurredAt;
    }
}
