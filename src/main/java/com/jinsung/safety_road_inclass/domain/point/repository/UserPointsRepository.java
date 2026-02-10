package com.jinsung.safety_road_inclass.domain.point.repository;

import com.jinsung.safety_road_inclass.domain.point.entity.UserPoints;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserPointsRepository - 유저 포인트 잔액 데이터 접근
 */
public interface UserPointsRepository extends JpaRepository<UserPoints, Long> {

    Optional<UserPoints> findByUserId(Long userId);
}
