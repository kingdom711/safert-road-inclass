package com.jinsung.safety_road_inclass.domain.gold.dto;

import com.jinsung.safety_road_inclass.domain.gold.entity.UserGold;
import lombok.Builder;
import lombok.Getter;

/**
 * GoldResponse - 골드 잔액 응답 DTO
 */
@Getter
@Builder
public class GoldResponse {

    private int balance;
    private int totalEarned;
    private int totalSpent;

    public static GoldResponse from(UserGold userGold) {
        return GoldResponse.builder()
                .balance(userGold.getBalance())
                .totalEarned(userGold.getTotalEarned())
                .totalSpent(userGold.getTotalSpent())
                .build();
    }

    public static GoldResponse empty() {
        return GoldResponse.builder()
                .balance(0)
                .totalEarned(0)
                .totalSpent(0)
                .build();
    }
}
