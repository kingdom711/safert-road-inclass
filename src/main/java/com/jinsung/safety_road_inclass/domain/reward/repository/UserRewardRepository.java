package com.jinsung.safety_road_inclass.domain.reward.repository;

import com.jinsung.safety_road_inclass.domain.reward.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.reward.entity.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserRewardRepository - 사용자 보상 저장소
 */
public interface UserRewardRepository extends JpaRepository<UserReward, Long> {

    List<UserReward> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserReward> findByStatusOrderByCreatedAtAsc(RewardStatus status);

    @Query("SELECT ur FROM UserReward ur JOIN FETCH ur.user JOIN FETCH ur.reward WHERE ur.status = :status ORDER BY ur.createdAt ASC")
    List<UserReward> findWithRewardByStatusOrderByCreatedAtAsc(@Param("status") RewardStatus status);

    List<UserReward> findAllByOrderByCreatedAtDesc();

    long countByStatus(RewardStatus status);

    @Query("SELECT ur.user.id, COUNT(ur), COALESCE(SUM(ur.goldPaid), 0), MAX(ur.createdAt) FROM UserReward ur " +
            "WHERE ur.status = :status GROUP BY ur.user.id")
    List<Object[]> aggregateByUserAndStatus(@Param("status") RewardStatus status);

    @Query("SELECT ur.reward.id, ur.reward.name, ur.reward.type, COUNT(ur), COALESCE(SUM(ur.goldPaid), 0), " +
            "COALESCE(SUM(ur.reward.cashValue), 0), ur.reward.remainingQuantity " +
            "FROM UserReward ur WHERE ur.status = :status " +
            "GROUP BY ur.reward.id, ur.reward.name, ur.reward.type, ur.reward.remainingQuantity " +
            "ORDER BY COUNT(ur) DESC, ur.reward.name ASC")
    List<Object[]> aggregateRewardDemandByStatus(@Param("status") RewardStatus status);

    @Query("SELECT ur.user.id, COUNT(ur), COALESCE(SUM(ur.goldPaid), 0), MAX(ur.createdAt) FROM UserReward ur " +
            "WHERE ur.createdAt >= :start AND ur.createdAt < :end GROUP BY ur.user.id")
    List<Object[]> aggregateByUserAndCreatedAtBetween(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);
}
