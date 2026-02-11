package com.jinsung.safety_road_inclass.domain.exchange.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ExchangeRequest - 포인트를 골드로 교환 요청 DTO
 */
@Getter
@NoArgsConstructor
public class ExchangeRequest {

    @NotNull(message = "포인트 금액은 필수입니다")
    @Min(value = 1, message = "포인트는 1 이상이어야 합니다")
    private Integer pointsAmount;
}
