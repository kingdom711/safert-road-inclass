package com.jinsung.safety_road_inclass.domain.reward.entity;

import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Reward - 보상 Entity (관리자가 등록하는 보상 아이템)
 */
@Entity
@Table(name = "rewards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reward extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "gold_price", nullable = false)
    private int goldPrice;

    @Column(name = "cash_value", nullable = false)
    private int cashValue;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RewardType type;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private int remainingQuantity;

    @Column(nullable = false)
    private boolean active = true;

    @Builder
    public Reward(String name, String description, int goldPrice, int cashValue,
            String imageUrl, RewardType type, int totalQuantity) {
        this.name = name;
        this.description = description;
        this.goldPrice = goldPrice;
        this.cashValue = cashValue;
        this.imageUrl = imageUrl;
        this.type = type;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = totalQuantity;
        this.active = true;
    }

    /**
     * 재고 차감
     */
    public void decreaseQuantity() {
        if (remainingQuantity <= 0) {
            throw new CustomException(ErrorCode.REWARD_OUT_OF_STOCK);
        }
        this.remainingQuantity--;
    }

    /**
     * 활성화 상태 토글
     */
    public void toggleActive() {
        this.active = !this.active;
    }
}
