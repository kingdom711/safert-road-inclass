package com.jinsung.safety_road_inclass.domain.auth.controller;

import com.jinsung.safety_road_inclass.domain.auth.dto.PasswordResetApprovalResponse;
import com.jinsung.safety_road_inclass.domain.auth.service.AuthService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/password-reset-requests")
@RequiredArgsConstructor
public class AdminPasswordResetController {

    private final AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PasswordResetApprovalResponse>>> getPasswordResetRequests(
            @RequestParam(defaultValue = "true") boolean pendingOnly) {
        return ResponseEntity.ok(ApiResponse.success(authService.getPasswordResetRequests(pendingOnly)));
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponse<PasswordResetApprovalResponse>> approvePasswordReset(
            @PathVariable Long requestId) {
        JwtTokenProvider.CustomUserPrincipal principal = getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(authService.approvePasswordReset(requestId, principal.userId())));
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<PasswordResetApprovalResponse>> rejectPasswordReset(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {
        JwtTokenProvider.CustomUserPrincipal principal = getPrincipal();
        String reason = body == null ? null : body.get("reason");
        return ResponseEntity.ok(ApiResponse.success(authService.rejectPasswordReset(requestId, principal.userId(), reason)));
    }

    private JwtTokenProvider.CustomUserPrincipal getPrincipal() {
        return (JwtTokenProvider.CustomUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
