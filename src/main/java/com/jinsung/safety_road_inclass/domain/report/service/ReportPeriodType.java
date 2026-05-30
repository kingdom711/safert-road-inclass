package com.jinsung.safety_road_inclass.domain.report.service;

public enum ReportPeriodType {
    WEEKLY,
    MONTHLY,
    YEARLY;

    public static ReportPeriodType from(String value) {
        if (value == null || value.isBlank()) {
            return WEEKLY;
        }

        return switch (value.trim().toLowerCase()) {
            case "week", "weekly" -> WEEKLY;
            case "month", "monthly" -> MONTHLY;
            case "year", "yearly", "annual" -> YEARLY;
            default -> throw new IllegalArgumentException("지원하지 않는 리포트 타입입니다: " + value);
        };
    }
}
