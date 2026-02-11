package com.jinsung.safety_road_inclass.domain.reward.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserReward - 사용자가 교환한 보상 내역 Entity
 */
@Entity
@Table(name = "user_rewards", indexes = {
        @Index(name = "idx_user_rewards_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserReward extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @Column(name = "gold_paid", nullable = false)
    private int goldPaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RewardStatus status;

    @Column(name = "coupon_code", length = 100)
    private String couponCode;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Builder
    public UserReward(User user, Reward reward, int goldPaid) {
        this.user = user;
        this.reward = reward;
        this.goldPaid = goldPaid;
        this.status = RewardStatus.PENDING;
    }

    /**
     * 발송 완료 처리
     */
    public void deliver(String couponCode) {
        this.status = RewardStatus.DELIVERED;
        this.couponCode = couponCode;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * 취소 처리
     */
    public void cancel() {
        this.status = RewardStatus.CANCELLED;
    }
}
