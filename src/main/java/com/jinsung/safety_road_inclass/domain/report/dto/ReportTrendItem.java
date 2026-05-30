package com.jinsung.safety_road_inclass.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTrendItem {

    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
    private long completedQuests;
    private long educationCompletions;
    private long loginDays;
    private long hazardReports;
    private long hazardCompletions;
    private int earnedPoints;
    private int activityScore;
}
