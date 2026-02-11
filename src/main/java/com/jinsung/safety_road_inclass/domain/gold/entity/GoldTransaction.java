package com.jinsung.safety_road_inclass.domain.gold.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GoldTransaction - 골드 거래 내역 Entity
 */
@Entity
@Table(name = "gold_transactions", indexes = {
        @Index(name = "idx_gold_txn_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoldTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GoldTransactionType type;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(length = 500)
    private String description;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Builder
    public GoldTransaction(User user, int amount, GoldTransactionType type, String reason, String description,
            int balanceAfter) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
        this.description = description;
        this.balanceAfter = balanceAfter;
    }
}
