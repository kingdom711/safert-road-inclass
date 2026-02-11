package com.jinsung.safety_road_inclass.domain.gameprofile.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.gameprofile.entity.UserGameProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameProfileRepository extends JpaRepository<UserGameProfile, Long> {
    Optional<UserGameProfile> findByUser(User user);

    Optional<UserGameProfile> findByUserId(Long userId);
}
