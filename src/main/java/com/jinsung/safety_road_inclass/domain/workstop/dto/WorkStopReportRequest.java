package com.jinsung.safety_road_inclass.domain.workstop.dto;

import com.jinsung.safety_road_inclass.domain.workstop.entity.HazardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkStopReportRequest {

    @NotNull(message = "위험 유형은 필수입니다.")
    private HazardType hazardType;

    @NotBlank(message = "위험 상황 설명은 필수입니다.")
    private String description;

    private String zone;

    private Boolean isAnonymous = false;

    private String photoUrl;
}
