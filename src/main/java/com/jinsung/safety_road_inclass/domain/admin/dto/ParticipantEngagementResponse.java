package com.jinsung.safety_road_inclass.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ParticipantEngagementResponse {

    private final LocalDate from;
    private final LocalDate to;
    private final Summary summary;
    private final List<Row> participants;

    @Getter
    @Builder
    public static class Summary {
        private final int totalParticipants;
        private final int activeParticipants;
        private final int inactiveParticipants;
        private final double averageEngagementScore;
        private final long totalEducationCompletions;
        private final long totalHazardReports;
        private final long totalWorkStopReports;
        private final long totalPointsEarned;
    }

    @Getter
    @Builder
    public static class Row {
        private final Long userId;
        private final String username;
        private final String name;
        private final String role;
        private final Long teamId;
        private final String teamName;
        private final long attendanceCount;
        private final long educationCompletions;
        private final double averageQuizScore;
        private final long questCompletions;
        private final long questProgressCount;
        private final long hazardReports;
        private final long hazardAcks;
        private final long workStopReports;
        private final long pointsEarned;
        private final LocalDateTime lastActivityAt;
        private final int engagementScore;
    }
}
