package com.jinsung.safety_road_inclass.domain.reward.controller;

import com.jinsung.safety_road_inclass.domain.reward.dto.RewardResponse;
import com.jinsung.safety_road_inclass.domain.reward.dto.UserRewardResponse;
import com.jinsung.safety_road_inclass.domain.reward.service.RewardService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RewardController - 보상센터 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    /**
     * 활성화된 보상 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<RewardResponse>> getRewards() {
        return ResponseEntity.ok(rewardService.getActiveRewards());
    }

    /**
     * 보상 교환
     */
    @PostMapping("/{rewardId}/exchange")
    public ResponseEntity<UserRewardResponse> exchangeReward(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long rewardId) {

        UserRewardResponse response = rewardService.exchangeReward(
                principal.userId(),
                rewardId);

        return ResponseEntity.ok(response);
    }

    /**
     * 내 보상 내역 조회
     */
    @GetMapping("/my-rewards")
    public ResponseEntity<List<UserRewardResponse>> getMyRewards(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                rewardService.getUserRewards(principal.userId()));
    }
}
