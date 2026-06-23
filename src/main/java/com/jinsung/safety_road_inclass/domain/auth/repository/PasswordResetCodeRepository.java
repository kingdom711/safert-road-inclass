package com.jinsung.safety_road_inclass.domain.auth.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.PasswordResetCode;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    Optional<PasswordResetCode> findFirstByUserOrderByCreatedAtDesc(User user);

    List<PasswordResetCode> findAllByUserAndUsedAtIsNull(User user);
}
