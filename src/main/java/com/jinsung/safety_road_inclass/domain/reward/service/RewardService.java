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

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                // [New] 상품 교환 시 본인인증 여부 확인
                // GIFT_CARD, VOUCHER 등 실물/쿠폰 상품은 인증 필요
                // (RewardType Enum 확인 필요, 여기서는 예시로 하드코딩하거나 타입 체크)
                // 만약 Reward 엔티티에 type이 있다면 아래와 같이 처리:
                // if (reward.getType() == RewardType.GIFT_CARD && !user.isVerified()) {
                // throw new CustomException(ErrorCode.USER_NOT_VERIFIED);
                // }
                // 우선 모든 보상에 대해 인증 체크 (또는 특정 조건)
                // 기획서상 "포인트를 상품과 교환할때는 신원인증" -> 골드는 제외?
                // RewardService에서는 골드로 상품 교환 로직이므로, 상품 교환 시 인증 필수
                if (!user.isVerified()) {
                        throw new CustomException(ErrorCode.USER_NOT_VERIFIED);
                }

                // 3. 골드 차감
                goldService.spendGold(userId, reward.getGoldPrice(),
                                "보상 교환", reward.getName());

                // 4. 사용자 보상 기록 생성

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
