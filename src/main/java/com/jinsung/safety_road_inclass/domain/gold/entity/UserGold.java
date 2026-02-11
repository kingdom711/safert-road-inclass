package com.jinsung.safety_road_inclass.domain.gold.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UserGold - 유저별 골드 잔액 Entity (1:1)
 */
@Entity
@Table(name = "user_gold")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGold extends BaseTimeEntity {

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

    public UserGold(User user) {
        this.user = user;
    }

    /**
     * 골드 적립
     */
    public void earn(int amount) {
        this.balance += amount;
        this.totalEarned += amount;
    }

    /**
     * 골드 차감
     */
    public void spend(int amount) {
        this.balance -= amount;
        this.totalSpent += amount;
    }
}
