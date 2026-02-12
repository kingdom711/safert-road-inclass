package com.jinsung.safety_road_inclass.domain.reward.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.gold.service.GoldService;
import com.jinsung.safety_road_inclass.domain.inventory.service.InventoryService;
import com.jinsung.safety_road_inclass.domain.reward.dto.RewardResponse;
import com.jinsung.safety_road_inclass.domain.reward.dto.UserRewardResponse;
import com.jinsung.safety_road_inclass.domain.reward.entity.Reward;
import com.jinsung.safety_road_inclass.domain.reward.entity.RewardStatus;
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
import java.util.UUID;
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
        private final InventoryService inventoryService;

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

        /**
         * 대기 중인 교환 요청 목록 조회 (관리자용)
         */
        public List<UserRewardResponse> getPendingRewards() {
                return userRewardRepository.findByStatusOrderByCreatedAtAsc(RewardStatus.PENDING)
                                .stream()
                                .map(UserRewardResponse::from)
                                .collect(Collectors.toList());
        }

        /**
         * 교환 요청 승인 (관리자) → 쿠폰코드 생성 + 인벤토리 전송
         */
        @Transactional
        public UserRewardResponse approveReward(Long userRewardId) {
                UserReward userReward = userRewardRepository.findById(userRewardId)
                                .orElseThrow(() -> new CustomException(ErrorCode.REWARD_NOT_FOUND));

                if (userReward.getStatus() != RewardStatus.PENDING) {
                        throw new CustomException(ErrorCode.REWARD_ALREADY_CLAIMED);
                }

                // 가상 쿠폰코드 생성
                String couponCode = generateCouponCode();
                userReward.deliver(couponCode);

                // 인벤토리에 기프티콘 아이템 추가
                Reward reward = userReward.getReward();
                inventoryService.addItemToInventory(
                                userReward.getUser().getId(),
                                "reward_" + reward.getId(),
                                1,
                                "reward_exchange");

                log.info("Reward approved: userRewardId={}, couponCode={}, userId={}",
                                userRewardId, couponCode, userReward.getUser().getId());

                return UserRewardResponse.from(userReward);
        }

        /**
         * 교환 요청 거절 (관리자) → 골드 환불
         */
        @Transactional
        public UserRewardResponse rejectReward(Long userRewardId) {
                UserReward userReward = userRewardRepository.findById(userRewardId)
                                .orElseThrow(() -> new CustomException(ErrorCode.REWARD_NOT_FOUND));

                if (userReward.getStatus() != RewardStatus.PENDING) {
                        throw new CustomException(ErrorCode.REWARD_ALREADY_CLAIMED);
                }

                userReward.cancel();

                // 골드 환불
                goldService.addGold(
                                userReward.getUser().getId(),
                                userReward.getGoldPaid(),
                                "보상 교환 거절 환불",
                                userReward.getReward().getName());

                log.info("Reward rejected: userRewardId={}, goldRefunded={}, userId={}",
                                userRewardId, userReward.getGoldPaid(), userReward.getUser().getId());

                return UserRewardResponse.from(userReward);
        }

        private String generateCouponCode() {
                String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
                return "GIFT-" + uuid.substring(0, 4) + "-" + uuid.substring(4, 8);
        }
}
