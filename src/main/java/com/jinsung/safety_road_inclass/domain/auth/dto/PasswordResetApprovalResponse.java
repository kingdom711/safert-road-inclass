package com.jinsung.safety_road_inclass.domain.auth.dto;

import com.jinsung.safety_road_inclass.domain.auth.entity.PasswordResetApprovalRequest;
import com.jinsung.safety_road_inclass.domain.auth.entity.PasswordResetApprovalStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PasswordResetApprovalResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String username;
    private String email;
    private PasswordResetApprovalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String rejectReason;

    public static PasswordResetApprovalResponse from(PasswordResetApprovalRequest request) {
        return PasswordResetApprovalResponse.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .userName(request.getUser().getName())
                .username(request.getUser().getUsername())
                .email(request.getRequestedEmail())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .processedAt(request.getProcessedAt())
                .rejectReason(request.getRejectReason())
                .build();
    }
}
