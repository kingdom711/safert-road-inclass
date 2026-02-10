package com.jinsung.safety_road_inclass.domain.point.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PointTransaction - 포인트 거래 내역 Entity
 */
@Entity
@Table(name = "point_transactions", indexes = {
        @Index(name = "idx_point_txn_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseTimeEntity {

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
    private TransactionType type;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(length = 500)
    private String description;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Builder
    public PointTransaction(User user, int amount, TransactionType type, String reason, String description, int balanceAfter) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
        this.description = description;
        this.balanceAfter = balanceAfter;
    }
}
