package com.jinsung.safety_road_inclass.domain.inventory.controller;

import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.domain.inventory.dto.BoxOpenedResponse;
import com.jinsung.safety_road_inclass.domain.inventory.dto.InventoryResponse;
import com.jinsung.safety_road_inclass.domain.inventory.entity.InventoryItemType;
import com.jinsung.safety_road_inclass.domain.inventory.service.InventoryService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * InventoryController - 인벤토리 API
 */
@Tag(name = "Inventory", description = "인벤토리 관리 API")
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "인벤토리 조회", description = "사용자의 인벤토리를 조회합니다. 타입별 필터링 가능합니다.")
    @GetMapping
    public ApiResponse<InventoryResponse> getInventory(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) InventoryItemType type) {

        InventoryResponse inventory = inventoryService.getInventory(principal.userId(), type);

        return ApiResponse.success(inventory);
    }

    @Operation(summary = "상자 열기", description = "인벤토리의 상자를 열어 아이템을 획득합니다.")
    @PostMapping("/boxes/{inventoryItemId}/open")
    public ApiResponse<BoxOpenedResponse> openBox(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long inventoryItemId) {

        BoxOpenedResponse response = inventoryService.openBox(principal.userId(), inventoryItemId);

        return ApiResponse.success(response);
    }
}
