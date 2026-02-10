package com.jinsung.safety_road_inclass.domain.point.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * AddPointsResponse - 포인트 적립/차감 응답 DTO
 */
@Getter
@Builder
public class AddPointsResponse {

    private int amount;
    private int balanceAfter;
    private String reason;
}
