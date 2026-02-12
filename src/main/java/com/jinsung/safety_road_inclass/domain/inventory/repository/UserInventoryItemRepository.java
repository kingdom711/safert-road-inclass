package com.jinsung.safety_road_inclass.domain.inventory.repository;

import com.jinsung.safety_road_inclass.domain.inventory.entity.InventoryItemType;
import com.jinsung.safety_road_inclass.domain.inventory.entity.UserInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * UserInventoryItemRepository - 사용자 인벤토리 데이터 접근
 */
public interface UserInventoryItemRepository extends JpaRepository<UserInventoryItem, Long> {

    /**
     * 사용자별 인벤토리 조회
     */
    List<UserInventoryItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자별 타입별 인벤토리 조회
     */
    List<UserInventoryItem> findByUserIdAndItemTypeOrderByCreatedAtDesc(
            Long userId, InventoryItemType itemType);

    /**
     * 사용자별 특정 아이템 조회
     */
    Optional<UserInventoryItem> findByUserIdAndItemIdAndItemType(
            Long userId, String itemId, InventoryItemType itemType);

    /**
     * 사용자별 상자 조회
     */
    List<UserInventoryItem> findByUserIdAndItemTypeOrderByCreatedAtDesc(
            Long userId, InventoryItemType itemType);
}
