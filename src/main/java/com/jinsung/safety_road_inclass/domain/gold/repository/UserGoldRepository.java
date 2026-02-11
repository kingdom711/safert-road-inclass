package com.jinsung.safety_road_inclass.domain.gold.repository;

import com.jinsung.safety_road_inclass.domain.gold.entity.UserGold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserGoldRepository - 유저 골드 저장소
 */
public interface UserGoldRepository extends JpaRepository<UserGold, Long> {

    Optional<UserGold> findByUserId(Long userId);
}
