package com.jinsung.safety_road_inclass.domain.workstop.dto;

import com.jinsung.safety_road_inclass.domain.workstop.entity.HazardType;
import com.jinsung.safety_road_inclass.domain.workstop.entity.ReportStatus;
import com.jinsung.safety_road_inclass.domain.workstop.entity.WorkStopReport;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class WorkStopReportResponse {

    private Long id;
    private String reporterName;
    private HazardType hazardType;
    private String hazardLabel;
    private String description;
    private String zone;
    private Boolean isAnonymous;
    private ReportStatus status;
    private String statusLabel;
    private String photoUrl;
    private String resolutionNote;
    private String actionPlan;
    private String actionMethod;
    private String resumeVerification;
    private Integer rewardPoints;
    private LocalDateTime resolvedAt;
    private LocalDateTime resumedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StatusHistoryItem> statusHistory;

    @Getter
    @Builder
    public static class StatusHistoryItem {
        private ReportStatus status;
        private String statusLabel;
        private LocalDateTime changedAt;
        private String changedByName;
        private String note;
    }

    public static WorkStopReportResponse from(WorkStopReport report) {
        return WorkStopReportResponse.builder()
                .id(report.getId())
                .reporterName(report.getIsAnonymous() ? "익명" : report.getReporter().getName())
                .hazardType(report.getHazardType())
                .hazardLabel(report.getHazardType().getLabel())
                .description(report.getDescription())
                .zone(report.getZone())
                .isAnonymous(report.getIsAnonymous())
                .status(report.getStatus())
                .statusLabel(report.getStatus().getLabel())
                .photoUrl(report.getPhotoUrl())
                .resolutionNote(report.getResolutionNote())
                .actionPlan(report.getActionPlan())
                .actionMethod(report.getActionMethod())
                .resumeVerification(report.getResumeVerification())
                .rewardPoints(report.getRewardPoints())
                .resolvedAt(report.getResolvedAt())
                .resumedAt(report.getResumedAt())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .statusHistory(report.getStatusHistories().stream()
                        .map(h -> StatusHistoryItem.builder()
                                .status(h.getStatus())
                                .statusLabel(h.getStatus().getLabel())
                                .changedAt(h.getChangedAt())
                                .changedByName(h.getChangedBy().getName())
                                .note(h.getNote())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
