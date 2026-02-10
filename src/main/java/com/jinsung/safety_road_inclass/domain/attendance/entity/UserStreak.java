package com.jinsung.safety_road_inclass.domain.attendance.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * UserStreak - 유저별 연속 출석 Entity (1:1)
 */
@Entity
@Table(name = "user_streaks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStreak extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak = 0;

    @Column(name = "last_check_in_date")
    private LocalDate lastCheckInDate;

    public UserStreak(User user) {
        this.user = user;
    }

    /**
     * 출석 체크인 시 스트릭 업데이트
     */
    public void checkIn(LocalDate today) {
        if (lastCheckInDate != null && lastCheckInDate.plusDays(1).equals(today)) {
            // 연속 출석
            this.currentStreak++;
        } else {
            // 연속 끊김 또는 첫 출석
            this.currentStreak = 1;
        }

        if (this.currentStreak > this.longestStreak) {
            this.longestStreak = this.currentStreak;
        }

        this.lastCheckInDate = today;
    }
}
