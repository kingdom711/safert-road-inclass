package com.jinsung.safety_road_inclass.domain.hazardcycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "동료 근로자 확인 응답")
public class HazardAckResponse {

    private Long id;
    private Long hazardReportId;
    private Long ackerId;
    private String ackerName;
    private String comment;
    private LocalDateTime ackedAt;
}
