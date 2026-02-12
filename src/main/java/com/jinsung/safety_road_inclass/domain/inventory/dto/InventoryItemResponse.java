package com.jinsung.safety_road_inclass.domain.inventory.dto;

import com.jinsung.safety_road_inclass.domain.inventory.entity.InventoryItemType;
import com.jinsung.safety_road_inclass.domain.inventory.entity.UserInventoryItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * InventoryItemResponse - 인벤토리 아이템 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class InventoryItemResponse {

    private Long id;
    private String itemId;
    private InventoryItemType itemType;
    private Integer quantity;
    private String source;
    private Long sourceId;
    private LocalDateTime obtainedAt;
    private LocalDateTime createdAt;

    public static InventoryItemResponse from(UserInventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .itemId(item.getItemId())
                .itemType(item.getItemType())
                .quantity(item.getQuantity())
                .source(item.getSource())
                .sourceId(item.getSourceId())
                .obtainedAt(item.getObtainedAt())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
