package com.jinsung.safety_road_inclass.domain.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * InventoryResponse - 인벤토리 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class InventoryResponse {

    private List<InventoryItemResponse> items;
    private List<InventoryItemResponse> boxes;
    private int totalItems;
    private int totalBoxes;

    public static InventoryResponse of(List<InventoryItemResponse> items, List<InventoryItemResponse> boxes) {
        return InventoryResponse.builder()
                .items(items)
                .boxes(boxes)
                .totalItems(items.size())
                .totalBoxes(boxes.size())
                .build();
    }
}
