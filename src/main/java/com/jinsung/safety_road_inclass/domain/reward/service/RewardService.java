package com.jinsung.safety_road_inclass.domain.reward.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.gold.service.GoldService;
import com.jinsung.safety_road_inclass.domain.reward.dto.RewardResponse;
import com.jinsung.safety_road_inclass.domain.reward.dto.UserRewardResponse;
import com.jinsung.safety_road_inclass.domain.reward.entity.Reward;
import com.jinsung.safety_road_inclass.domain.reward.entity.UserReward;
import com.jinsung.safety_road_inclass.domain.reward.repository.RewardRepository;
import com.jinsung.safety_road_inclass.domain.reward.repository.UserRewardRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RewardService - 보상센터 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RewardService {

    private final RewardRepository rewardRepository;
    private final UserRewardRepository userRewardRepository;
    private final UserRepository userRepository;
    private final GoldService goldService;

    /**
     * 활성화된 보상 목록 조회
     */
    public List<RewardResponse> getActiveRewards() {
        return rewardRepository.findByActiveTrue()
                .stream()
                .map(RewardResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 보상 교환 (골드로 쿠폰 구매)
     */
    @Transactional
    public UserRewardResponse exchangeReward(Long userId, Long rewardId) {
        // 1. 보상 조회 및 검증
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new CustomException(ErrorCode.REWARD_NOT_FOUND));

        if (!reward.isActive()) {
            throw new CustomException(ErrorCode.REWARD_NOT_ACTIVE);
        }

        // 2. 재고 확인 및 차감
        reward.decreaseQuantity();

        // 3. 골드 차감
        goldService.spendGold(userId, reward.getGoldPrice(),
                "보상 교환", reward.getName());

        // 4. 사용자 보상 기록 생성
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserReward userReward = UserReward.builder()
                .user(user)
                .reward(reward)
                .goldPaid(reward.getGoldPrice())
                .build();

        userRewardRepository.save(userReward);

        log.info("Reward exchanged: userId={}, rewardId={}, goldPaid={}",
                userId, rewardId, reward.getGoldPrice());

        return UserRewardResponse.from(userReward);
    }

    /**
     * 사용자 보상 내역 조회
     */
    public List<UserRewardResponse> getUserRewards(Long userId) {
        return userRewardRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(UserRewardResponse::from)
                .collect(Collectors.toList());
    }
}
