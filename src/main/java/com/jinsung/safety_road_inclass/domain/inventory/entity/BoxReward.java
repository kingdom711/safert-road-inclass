package com.jinsung.safety_road_inclass.domain.inventory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * BoxReward - 상자 보상 정의 Entity
 */
@Entity
@Table(name = "box_rewards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoxReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    private ItemBox box;

    @Column(name = "item_id", nullable = false, length = 50)
    private String itemId;

    @Column(nullable = false)
    private Integer weight = 100;

    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity = 1;

    @Column(name = "max_quantity", nullable = false)
    private Integer maxQuantity = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public BoxReward(ItemBox box, String itemId, Integer weight, Integer minQuantity, Integer maxQuantity) {
        this.box = box;
        this.itemId = itemId;
        this.weight = weight != null ? weight : 100;
        this.minQuantity = minQuantity != null ? minQuantity : 1;
        this.maxQuantity = maxQuantity != null ? maxQuantity : 1;
    }

    /**
     * Box 설정 (양방향 관계용)
     */
    void setBox(ItemBox box) {
        this.box = box;
    }
}
