package com.jinsung.safety_road_inclass.domain.inventory.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserInventoryItem - 사용자 인벤토리 아이템 Entity
 */
@Entity
@Table(name = "user_inventory_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInventoryItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_id", nullable = false, length = 50)
    private String itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private InventoryItemType itemType;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(length = 50)
    private String source;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "obtained_at")
    private LocalDateTime obtainedAt;

    @Builder
    public UserInventoryItem(User user, String itemId, InventoryItemType itemType,
                            Integer quantity, String source, Long sourceId) {
        this.user = user;
        this.itemId = itemId;
        this.itemType = itemType;
        this.quantity = quantity != null ? quantity : 1;
        this.source = source;
        this.sourceId = sourceId;
        this.obtainedAt = LocalDateTime.now();
    }

    /**
     * 수량 증가
     */
    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    /**
     * 수량 감소
     */
    public void subtractQuantity(int amount) {
        if (this.quantity < amount) {
            throw new IllegalStateException("수량이 부족합니다.");
        }
        this.quantity -= amount;
    }
}
