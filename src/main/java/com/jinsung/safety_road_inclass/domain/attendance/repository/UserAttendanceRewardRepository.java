package com.jinsung.safety_road_inclass.domain.attendance.repository;

import com.jinsung.safety_road_inclass.domain.attendance.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.attendance.entity.UserAttendanceReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * UserAttendanceRewardRepository - 사용자 출석 보상 수령 데이터 접근
 */
public interface UserAttendanceRewardRepository extends JpaRepository<UserAttendanceReward, Long> {

    /**
     * 사용자별 수령 가능한 보상 조회
     */
    List<UserAttendanceReward> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, RewardStatus status);

    /**
     * 사용자별 특정 년월의 보상 조회
     */
    List<UserAttendanceReward> findByUserIdAndYearAndMonthOrderByCreatedAtDesc(
            Long userId, Integer year, Integer month);

    /**
     * 사용자별 특정 년월의 수령 가능한 보상 조회
     */
    List<UserAttendanceReward> findByUserIdAndYearAndMonthAndStatusOrderByCreatedAtDesc(
            Long userId, Integer year, Integer month, RewardStatus status);

    /**
     * 사용자, 보상 ID, 년월로 조회 (중복 체크용)
     */
    Optional<UserAttendanceReward> findByUserIdAndAttendanceRewardIdAndYearAndMonth(
            Long userId, Long attendanceRewardId, Integer year, Integer month);
}
