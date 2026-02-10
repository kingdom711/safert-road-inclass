package com.jinsung.safety_road_inclass.domain.point.dto;

import com.jinsung.safety_road_inclass.domain.point.entity.UserPoints;
import lombok.Builder;
import lombok.Getter;

/**
 * PointsResponse - 포인트 잔액 조회 응답 DTO
 */
@Getter
@Builder
public class PointsResponse {

    private int balance;
    private int totalEarned;
    private int totalSpent;

    public static PointsResponse from(UserPoints userPoints) {
        return PointsResponse.builder()
                .balance(userPoints.getBalance())
                .totalEarned(userPoints.getTotalEarned())
                .totalSpent(userPoints.getTotalSpent())
                .build();
    }

    /**
     * 포인트 기록이 없는 경우 기본값
     */
    public static PointsResponse empty() {
        return PointsResponse.builder()
                .balance(0)
                .totalEarned(0)
                .totalSpent(0)
                .build();
    }
}
