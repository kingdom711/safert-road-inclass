package com.jinsung.safety_road_inclass.domain.gold.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * AddGoldResponse - 골드 적립/차감 응답 DTO
 */
@Getter
@Builder
public class AddGoldResponse {

    private int amount;
    private int balanceAfter;
    private String reason;
}
