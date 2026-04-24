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
@Schema(description = "Hazard Cycle 원본 검증 응답(QR 검증용 공개 엔드포인트)")
public class HazardVerifyResponse {

    private boolean valid;
    private Long cycleId;
    private String certNumber;
    private String integrityHash;
    private String prevHash;
    private String reporterName;
    private LocalDateTime reportedAt;
    private String riskLevel;
    private Integer riskScore;
    private String status;
}
