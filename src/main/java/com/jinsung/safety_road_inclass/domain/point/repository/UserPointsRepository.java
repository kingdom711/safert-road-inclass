package com.jinsung.safety_road_inclass.domain.point.repository;

import com.jinsung.safety_road_inclass.domain.point.entity.UserPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * UserPointsRepository - 유저 포인트 잔액 데이터 접근
 */
public interface UserPointsRepository extends JpaRepository<UserPoints, Long> {

    Optional<UserPoints> findByUserId(Long userId);

    /**
     * 포인트 내림차순 정렬 (랭킹용)
     */
    @Query("SELECT up FROM UserPoints up JOIN FETCH up.user ORDER BY up.balance DESC")
    List<UserPoints> findAllOrderByBalanceDesc();
}
