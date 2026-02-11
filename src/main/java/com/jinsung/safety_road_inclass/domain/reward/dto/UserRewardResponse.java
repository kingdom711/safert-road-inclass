package com.jinsung.safety_road_inclass.domain.reward.dto;

import com.jinsung.safety_road_inclass.domain.reward.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.reward.entity.RewardType;
import com.jinsung.safety_road_inclass.domain.reward.entity.UserReward;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * UserRewardResponse - 사용자 보상 응답 DTO
 */
@Getter
@Builder
public class UserRewardResponse {

    private Long id;
    private Long rewardId;
    private String rewardName;
    private String rewardDescription;
    private int cashValue;
    private String imageUrl;
    private RewardType rewardType;
    private int goldPaid;
    private RewardStatus status;
    private String couponCode;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;

    public static UserRewardResponse from(UserReward userReward) {
        return UserRewardResponse.builder()
                .id(userReward.getId())
                .rewardId(userReward.getReward().getId())
                .rewardName(userReward.getReward().getName())
                .rewardDescription(userReward.getReward().getDescription())
                .cashValue(userReward.getReward().getCashValue())
                .imageUrl(userReward.getReward().getImageUrl())
                .rewardType(userReward.getReward().getType())
                .goldPaid(userReward.getGoldPaid())
                .status(userReward.getStatus())
                .couponCode(userReward.getCouponCode())
                .deliveredAt(userReward.getDeliveredAt())
                .createdAt(userReward.getCreatedAt())
                .build();
    }
}
