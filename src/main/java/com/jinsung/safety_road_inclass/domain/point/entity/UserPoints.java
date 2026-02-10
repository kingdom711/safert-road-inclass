package com.jinsung.safety_road_inclass.domain.point.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UserPoints - 유저별 포인트 잔액 Entity (1:1)
 */
@Entity
@Table(name = "user_points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPoints extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private int balance = 0;

    @Column(name = "total_earned", nullable = false)
    private int totalEarned = 0;

    @Column(name = "total_spent", nullable = false)
    private int totalSpent = 0;

    public UserPoints(User user) {
        this.user = user;
    }

    /**
     * 포인트 적립
     */
    public void earn(int amount) {
        this.balance += amount;
        this.totalEarned += amount;
    }

    /**
     * 포인트 차감
     */
    public void spend(int amount) {
        this.balance -= amount;
        this.totalSpent += amount;
    }
}
