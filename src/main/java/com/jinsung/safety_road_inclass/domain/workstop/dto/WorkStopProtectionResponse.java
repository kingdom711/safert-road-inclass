package com.jinsung.safety_road_inclass.domain.workstop.dto;

import com.jinsung.safety_road_inclass.domain.workstop.entity.WorkStopProtection;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WorkStopProtectionResponse {

    private Long id;
    private String workerName;
    private Long reportId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private boolean isCurrentlyActive;

    public static WorkStopProtectionResponse from(WorkStopProtection protection) {
        return WorkStopProtectionResponse.builder()
                .id(protection.getId())
                .workerName(protection.getWorker().getName())
                .reportId(protection.getWorkStopReport().getId())
                .startDate(protection.getStartDate())
                .endDate(protection.getEndDate())
                .isActive(protection.getIsActive())
                .isCurrentlyActive(protection.isCurrentlyActive())
                .build();
    }
}
