package com.jinsung.safety_road_inclass.domain.inventory.repository;

import com.jinsung.safety_road_inclass.domain.inventory.entity.BoxReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * BoxRewardRepository - 상자 보상 정의 데이터 접근
 */
public interface BoxRewardRepository extends JpaRepository<BoxReward, Long> {

    /**
     * 상자별 보상 목록 조회
     */
    List<BoxReward> findByBoxIdOrderByWeightDesc(Long boxId);
}
