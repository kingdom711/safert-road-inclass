package com.jinsung.safety_road_inclass.domain.activity.dto;

import com.jinsung.safety_road_inclass.domain.activity.entity.ActivityLog;
import com.jinsung.safety_road_inclass.domain.activity.entity.ActivityType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * ActivityLogResponse - 활동 기록 응답 DTO
 */
@Getter
@Builder
public class ActivityLogResponse {

    private Long id;
    private ActivityType activityType;
    private String title;
    private String description;
    private String metadata;
    private LocalDateTime createdAt;

    public static ActivityLogResponse from(ActivityLog activityLog) {
        return ActivityLogResponse.builder()
                .id(activityLog.getId())
                .activityType(activityLog.getActivityType())
                .title(activityLog.getTitle())
                .description(activityLog.getDescription())
                .metadata(activityLog.getMetadata())
                .createdAt(activityLog.getCreatedAt())
                .build();
    }
}
