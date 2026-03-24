package com.jinsung.safety_road_inclass.domain.workstop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RetaliationReportRequest {

    @NotNull(message = "보호 기간 ID는 필수입니다.")
    private Long protectionId;

    @NotBlank(message = "보복 유형은 필수입니다.")
    private String retaliationType;

    @NotBlank(message = "보복 상세 설명은 필수입니다.")
    private String description;

    private String evidenceUrl;
}
