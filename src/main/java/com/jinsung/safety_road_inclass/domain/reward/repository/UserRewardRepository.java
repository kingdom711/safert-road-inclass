package com.jinsung.safety_road_inclass.domain.reward.repository;

import com.jinsung.safety_road_inclass.domain.reward.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.reward.entity.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * UserRewardRepository - 사용자 보상 저장소
 */
public interface UserRewardRepository extends JpaRepository<UserReward, Long> {

    List<UserReward> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserReward> findByStatusOrderByCreatedAtAsc(RewardStatus status);
}
