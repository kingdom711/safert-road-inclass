package com.jinsung.safety_road_inclass.domain.attendance.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserAttendanceReward - 사용자 출석 보상 수령 Entity
 */
@Entity
@Table(name = "user_attendance_rewards", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "attendance_reward_id", "year", "month"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAttendanceReward extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_reward_id", nullable = false)
    private AttendanceReward attendanceReward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_record_id")
    private AttendanceRecord attendanceRecord;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RewardStatus status = RewardStatus.AVAILABLE;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Builder
    public UserAttendanceReward(User user, AttendanceReward attendanceReward,
                               AttendanceRecord attendanceRecord, Integer year, Integer month,
                               RewardStatus status) {
        this.user = user;
        this.attendanceReward = attendanceReward;
        this.attendanceRecord = attendanceRecord;
        this.year = year;
        this.month = month;
        this.status = status != null ? status : RewardStatus.AVAILABLE;
    }

    /**
     * 보상 수령 처리
     */
    public void claim() {
        this.status = RewardStatus.CLAIMED;
        this.claimedAt = LocalDateTime.now();
    }

    /**
     * 만료 처리
     */
    public void expire() {
        this.status = RewardStatus.EXPIRED;
    }
}
