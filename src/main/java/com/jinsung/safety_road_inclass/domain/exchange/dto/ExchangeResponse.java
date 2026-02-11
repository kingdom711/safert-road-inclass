package com.jinsung.safety_road_inclass.domain.exchange.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * ExchangeResponse - 포인트-골드 교환 응답 DTO
 */
@Getter
@Builder
public class ExchangeResponse {

    private int pointsSpent;
    private int goldEarned;
    private int exchangeRate;
}
