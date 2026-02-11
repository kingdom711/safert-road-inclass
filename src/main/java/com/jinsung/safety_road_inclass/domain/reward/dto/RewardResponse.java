package com.jinsung.safety_road_inclass.domain.reward.dto;

import com.jinsung.safety_road_inclass.domain.reward.entity.Reward;
import com.jinsung.safety_road_inclass.domain.reward.entity.RewardType;
import lombok.Builder;
import lombok.Getter;

/**
 * RewardResponse - 보상 응답 DTO
 */
@Getter
@Builder
public class RewardResponse {

    private Long id;
    private String name;
    private String description;
    private int goldPrice;
    private int cashValue;
    private String imageUrl;
    private RewardType type;
    private int totalQuantity;
    private int remainingQuantity;
    private boolean active;

    public static RewardResponse from(Reward reward) {
        return RewardResponse.builder()
                .id(reward.getId())
                .name(reward.getName())
                .description(reward.getDescription())
                .goldPrice(reward.getGoldPrice())
                .cashValue(reward.getCashValue())
                .imageUrl(reward.getImageUrl())
                .type(reward.getType())
                .totalQuantity(reward.getTotalQuantity())
                .remainingQuantity(reward.getRemainingQuantity())
                .active(reward.isActive())
                .build();
    }
}
