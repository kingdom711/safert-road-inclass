package com.jinsung.safety_road_inclass.domain.reward.repository;

import com.jinsung.safety_road_inclass.domain.reward.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.reward.entity.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
