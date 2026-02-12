package com.jinsung.safety_road_inclass.domain.attendance.repository;

import com.jinsung.safety_road_inclass.domain.attendance.entity.AttendanceReward;
import com.jinsung.safety_road_inclass.domain.attendance.entity.AttendanceRewardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * AttendanceRewardRepository - 출석 보상 마스터 데이터 접근
 */
public interface AttendanceRewardRepository extends JpaRepository<AttendanceReward, Long> {

    /**
     * 일차별 보상 조회
     */
    Optional<AttendanceReward> findByDayNumberAndActiveTrue(Integer dayNumber);

    /**
     * 활성화된 모든 보상 조회 (일차 순)
     */
    List<AttendanceReward> findByActiveTrueOrderByDayNumberAsc();

    /**
     * 일차 및 타입별 보상 조회
     */
    Optional<AttendanceReward> findByDayNumberAndRewardTypeAndActiveTrue(
            Integer dayNumber, AttendanceRewardType rewardType);
}
