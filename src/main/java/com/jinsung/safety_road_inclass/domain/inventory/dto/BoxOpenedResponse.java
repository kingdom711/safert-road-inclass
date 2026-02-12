package com.jinsung.safety_road_inclass.domain.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * BoxOpenedResponse - 상자 열기 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class BoxOpenedResponse {

    private Long boxId;
    private String boxName;
    private List<RewardedItem> rewardedItems;
    private String message;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RewardedItem {
        private String itemId;
        private Integer quantity;
    }

    public static BoxOpenedResponse of(Long boxId, String boxName, List<RewardedItem> rewardedItems) {
        return BoxOpenedResponse.builder()
                .boxId(boxId)
                .boxName(boxName)
                .rewardedItems(rewardedItems)
                .message("상자를 열었습니다!")
                .build();
    }
}
