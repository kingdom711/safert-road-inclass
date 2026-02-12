package com.jinsung.safety_road_inclass.domain.reward.controller;

import com.jinsung.safety_road_inclass.domain.reward.dto.UserRewardResponse;
import com.jinsung.safety_road_inclass.domain.reward.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminRewardController - 관리자 보상 승인/거절 API
 *
 * SecurityConfig에서 /api/v1/admin/** 경로는
 * ROLE_SAFETY_MANAGER, ROLE_ADMIN만 접근 가능
 */
@RestController
@RequestMapping("/api/v1/admin/rewards")
@RequiredArgsConstructor
public class AdminRewardController {

    private final RewardService rewardService;

    /**
     * 대기 중인 교환 요청 목록 조회
     */
    @GetMapping("/pending")
    public ResponseEntity<List<UserRewardResponse>> getPendingRewards() {
        return ResponseEntity.ok(rewardService.getPendingRewards());
    }

    /**
     * 교환 요청 승인 → 쿠폰코드 생성 + 인벤토리 전송
     */
    @PostMapping("/{userRewardId}/approve")
    public ResponseEntity<UserRewardResponse> approveReward(
            @PathVariable Long userRewardId) {
        return ResponseEntity.ok(rewardService.approveReward(userRewardId));
    }

    /**
     * 교환 요청 거절 → 골드 환불
     */
    @PostMapping("/{userRewardId}/reject")
    public ResponseEntity<UserRewardResponse> rejectReward(
            @PathVariable Long userRewardId) {
        return ResponseEntity.ok(rewardService.rejectReward(userRewardId));
    }
}
