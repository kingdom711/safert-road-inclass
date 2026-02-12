package com.jinsung.safety_road_inclass.domain.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * RewardClaimedResponse - 보상 수령 완료 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class RewardClaimedResponse {

    private Long rewardId;
    private Long inventoryItemId;
    private String message;

    public static RewardClaimedResponse of(Long rewardId, Long inventoryItemId) {
        return RewardClaimedResponse.builder()
                .rewardId(rewardId)
                .inventoryItemId(inventoryItemId)
                .message("보상을 수령했습니다.")
                .build();
    }
}
