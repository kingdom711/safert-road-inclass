package com.jinsung.safety_road_inclass.domain.attendance.entity;

import com.jinsung.safety_road_inclass.domain.inventory.entity.ItemBox;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AttendanceReward - 출석 보상 마스터 Entity
 */
@Entity
@Table(name = "attendance_rewards", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"day_number", "reward_type"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceReward extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 20)
    private AttendanceRewardType rewardType;

    @Column(name = "points_amount")
    private Integer pointsAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    private ItemBox box;

    @Column(name = "is_perfect_attendance", nullable = false)
    private Boolean isPerfectAttendance = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Builder
    public AttendanceReward(Integer dayNumber, AttendanceRewardType rewardType,
                           Integer pointsAmount, ItemBox box, Boolean isPerfectAttendance, Boolean active) {
        this.dayNumber = dayNumber;
        this.rewardType = rewardType;
        this.pointsAmount = pointsAmount;
        this.box = box;
        this.isPerfectAttendance = isPerfectAttendance != null ? isPerfectAttendance : false;
        this.active = active != null ? active : true;
    }
}
