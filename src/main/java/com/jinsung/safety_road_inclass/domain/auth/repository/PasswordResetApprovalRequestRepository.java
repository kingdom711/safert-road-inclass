package com.jinsung.safety_road_inclass.domain.auth.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.PasswordResetApprovalRequest;
import com.jinsung.safety_road_inclass.domain.auth.entity.PasswordResetApprovalStatus;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordResetApprovalRequestRepository extends JpaRepository<PasswordResetApprovalRequest, Long> {

    List<PasswordResetApprovalRequest> findAllByStatusOrderByCreatedAtDesc(PasswordResetApprovalStatus status);

    List<PasswordResetApprovalRequest> findAllByOrderByCreatedAtDesc();

    List<PasswordResetApprovalRequest> findAllByUserAndStatus(User user, PasswordResetApprovalStatus status);
}
