package com.jinsung.safety_road_inclass.domain.point.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AddPointsRequest - 포인트 적립/차감 요청 DTO
 */
@Getter
@NoArgsConstructor
public class AddPointsRequest {

    @NotNull(message = "포인트 금액은 필수입니다.")
    @Min(value = 1, message = "포인트 금액은 1 이상이어야 합니다.")
    private Integer amount;

    @NotBlank(message = "사유는 필수입니다.")
    private String reason;

    private String description;
}
