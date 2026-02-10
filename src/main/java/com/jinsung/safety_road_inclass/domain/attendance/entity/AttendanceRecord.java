package com.jinsung.safety_road_inclass.domain.attendance.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * AttendanceRecord - 일별 출석 기록 Entity
 */
@Entity
@Table(name = "attendance_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "check_in_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "points_earned", nullable = false)
    private int pointsEarned = 0;

    @Column(name = "streak_at_time", nullable = false)
    private int streakAtTime = 1;

    @Builder
    public AttendanceRecord(User user, LocalDate checkInDate, int pointsEarned, int streakAtTime) {
        this.user = user;
        this.checkInDate = checkInDate;
        this.pointsEarned = pointsEarned;
        this.streakAtTime = streakAtTime;
    }
}
