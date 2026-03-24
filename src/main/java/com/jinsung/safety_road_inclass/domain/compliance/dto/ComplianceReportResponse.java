package com.jinsung.safety_road_inclass.domain.compliance.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinsung.safety_road_inclass.domain.compliance.entity.ComplianceReport;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 컴플라이언스 리포트 응답 DTO
 */
@Getter
@Builder
public class ComplianceReportResponse {

    private Long id;

    // 기간 정보
    private Integer reportYear;
    private Integer reportMonth;
    private String periodLabel;

    // 교육 이수
    private EducationSummary education;

    // 위험성 평가
    private RiskAssessmentSummary riskAssessment;

    // 작업중지권
    private WorkStopSummary workStop;

    // 체크리스트
    private ChecklistSummary checklist;

    // 사고 통계
    private IncidentSummary incidents;

    // 종합
    private Integer complianceScore;
    private String complianceGrade;
    private List<String> recommendations;

    private LocalDateTime generatedAt;
    private String generatedByName;

    @Getter
    @Builder
    public static class EducationSummary {
        private Integer required;
        private Integer completed;
        private Integer rate;
        private Double cumulativeLegalHours;
    }

    @Getter
    @Builder
    public static class RiskAssessmentSummary {
        private Integer conducted;
        private Integer findings;
        private Integer resolved;
        private Integer resolvedRate;
    }

    @Getter
    @Builder
    public static class WorkStopSummary {
        private Integer exercised;
    }

    @Getter
    @Builder
    public static class ChecklistSummary {
        private Integer required;
        private Integer submitted;
        private Integer rate;
    }

    @Getter
    @Builder
    public static class IncidentSummary {
        private Integer total;
        private Map<String, Integer> byType;
        private Map<String, Integer> bySeverity;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static ComplianceReportResponse from(ComplianceReport report) {
        return ComplianceReportResponse.builder()
                .id(report.getId())
                .reportYear(report.getReportYear())
                .reportMonth(report.getReportMonth())
                .periodLabel(report.getReportYear() + "년 " + report.getReportMonth() + "월")
                .education(EducationSummary.builder()
                        .required(report.getEducationRequired())
                        .completed(report.getEducationCompleted())
                        .rate(report.getEducationRate())
                        .cumulativeLegalHours(report.getCumulativeLegalHours())
                        .build())
                .riskAssessment(RiskAssessmentSummary.builder()
                        .conducted(report.getRiskAssessmentConducted())
                        .findings(report.getRiskFindings())
                        .resolved(report.getRiskResolved())
                        .resolvedRate(report.getRiskResolvedRate())
                        .build())
                .workStop(WorkStopSummary.builder()
                        .exercised(report.getWorkStopExercised())
                        .build())
                .checklist(ChecklistSummary.builder()
                        .required(report.getChecklistRequired())
                        .submitted(report.getChecklistSubmitted())
                        .rate(report.getChecklistRate())
                        .build())
                .incidents(IncidentSummary.builder()
                        .total(report.getIncidentTotal())
                        .byType(parseJsonMap(report.getIncidentByType()))
                        .bySeverity(parseJsonMap(report.getIncidentBySeverity()))
                        .build())
                .complianceScore(report.getComplianceScore())
                .complianceGrade(determineGrade(report.getComplianceScore()))
                .recommendations(parseJsonList(report.getRecommendations()))
                .generatedAt(report.getCreatedAt())
                .generatedByName(report.getGeneratedBy() != null ? report.getGeneratedBy().getName() : "시스템")
                .build();
    }

    private static String determineGrade(int score) {
        if (score >= 90) return "A (우수)";
        if (score >= 80) return "B (양호)";
        if (score >= 70) return "C (보통)";
        if (score >= 60) return "D (미흡)";
        return "F (위험)";
    }

    private static Map<String, Integer> parseJsonMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return MAPPER.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private static List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
