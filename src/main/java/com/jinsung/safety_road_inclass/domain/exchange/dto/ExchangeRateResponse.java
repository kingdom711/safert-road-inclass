package com.jinsung.safety_road_inclass.domain.exchange.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * ExchangeRateResponse - 교환 비율 응답 DTO
 */
@Getter
@Builder
public class ExchangeRateResponse {

    private int pointsPerGold;
    private int minimumPoints;
}
