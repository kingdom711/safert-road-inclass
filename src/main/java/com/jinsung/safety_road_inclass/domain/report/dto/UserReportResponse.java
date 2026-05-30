package com.jinsung.safety_road_inclass.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReportResponse {

    private String periodType;
    private String periodLabel;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private ReportSummaryResponse summary;
    private List<ReportCategoryScore> categoryScores;
    private List<ReportTrendItem> trends;
    private List<String> strengths;
    private List<String> improvements;
    private List<String> recommendations;
}
