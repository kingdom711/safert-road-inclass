package com.jinsung.safety_road_inclass.domain.gameprofile.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.gameprofile.entity.UserGameProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameProfileRepository extends JpaRepository<UserGameProfile, Long> {
    Optional<UserGameProfile> findByUser(User user);

    Optional<UserGameProfile> findByUserId(Long userId);

    /**
     * 레벨 내림차순 정렬 (랭킹용)
     */
    @Query("SELECT gp FROM UserGameProfile gp JOIN FETCH gp.user ORDER BY gp.level DESC, gp.exp DESC")
    List<UserGameProfile> findAllOrderByLevelDesc();
}
