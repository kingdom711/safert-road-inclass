package com.jinsung.safety_road_inclass.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryResponse {

    private long completedQuests;
    private long educationCompletions;
    private long passedEducations;
    private int averageQuizScore;
    private long loginDays;
    private long hazardReports;
    private long hazardCompletions;
    private int earnedPoints;
    private int spentPoints;
    private int overallScore;
    private int scoreChange;
}
