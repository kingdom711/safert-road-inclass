package com.jinsung.safety_road_inclass.domain.workstop.entity;

/**
 * ReportStatus - 작업중지 신고 상태
 *
 * 산업안전보건법 제52조(근로자의 작업중지) 처리 절차 기반
 */
public enum ReportStatus {
    REPORTED("신고 완료"),
    CONFIRMED("접수 확인"),
    INVESTIGATING("조사 중"),
    RESOLVED("위험 해결"),
    RESUMED("작업 재개");

    private final String label;

    ReportStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
