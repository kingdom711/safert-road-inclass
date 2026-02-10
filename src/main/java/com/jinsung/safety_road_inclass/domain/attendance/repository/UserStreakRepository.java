package com.jinsung.safety_road_inclass.domain.attendance.repository;

import com.jinsung.safety_road_inclass.domain.attendance.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserStreakRepository - 유저 스트릭 데이터 접근
 */
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

    Optional<UserStreak> findByUserId(Long userId);
}
