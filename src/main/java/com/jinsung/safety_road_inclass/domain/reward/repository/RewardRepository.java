package com.jinsung.safety_road_inclass.domain.reward.repository;

import com.jinsung.safety_road_inclass.domain.reward.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * RewardRepository - 보상 저장소
 */
public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByActiveTrue();
}
