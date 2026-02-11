package com.jinsung.safety_road_inclass.domain.gameprofile.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.gameprofile.entity.UserSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSpecializationRepository extends JpaRepository<UserSpecialization, Long> {
    List<UserSpecialization> findByUser(User user);

    List<UserSpecialization> findByUserId(Long userId);

    Optional<UserSpecialization> findByUserAndSpecId(User user, String specId);

    Optional<UserSpecialization> findByUserIdAndSpecId(Long userId, String specId);
}
