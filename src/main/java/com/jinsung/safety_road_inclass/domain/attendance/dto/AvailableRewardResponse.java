package com.jinsung.safety_road_inclass.domain.attendance.dto;

import com.jinsung.safety_road_inclass.domain.attendance.entity.AttendanceRewardType;
import com.jinsung.safety_road_inclass.domain.attendance.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.attendance.entity.UserAttendanceReward;
import com.jinsung.safety_road_inclass.domain.inventory.entity.BoxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * AvailableRewardResponse - 수령 가능한 보상 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class AvailableRewardResponse {

    private Long id;
    private Long attendanceRewardId;
    private Integer dayNumber;
    private AttendanceRewardType rewardType;
    private Integer pointsAmount;
    private Long boxId;
    private String boxName;
    private BoxType boxType;
    private Boolean isPerfectAttendance;
    private RewardStatus status;
    private Integer year;
    private Integer month;

    public static AvailableRewardResponse from(UserAttendanceReward userReward) {
        var reward = userReward.getAttendanceReward();
        return AvailableRewardResponse.builder()
                .id(userReward.getId())
                .attendanceRewardId(reward.getId())
                .dayNumber(reward.getDayNumber())
                .rewardType(reward.getRewardType())
                .pointsAmount(reward.getPointsAmount())
                .boxId(reward.getBox() != null ? reward.getBox().getId() : null)
                .boxName(reward.getBox() != null ? reward.getBox().getName() : null)
                .boxType(reward.getBox() != null ? reward.getBox().getBoxType() : null)
                .isPerfectAttendance(reward.getIsPerfectAttendance())
                .status(userReward.getStatus())
                .year(userReward.getYear())
                .month(userReward.getMonth())
                .build();
    }
}
