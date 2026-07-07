package com.jinsung.safety_road_inclass.domain.point.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.point.dto.AddPointsResponse;
import com.jinsung.safety_road_inclass.domain.point.dto.PointTransactionResponse;
import com.jinsung.safety_road_inclass.domain.point.dto.PointsResponse;
import com.jinsung.safety_road_inclass.domain.point.entity.PointTransaction;
import com.jinsung.safety_road_inclass.domain.point.entity.TransactionType;
import com.jinsung.safety_road_inclass.domain.point.entity.UserPoints;
import com.jinsung.safety_road_inclass.domain.point.repository.PointTransactionRepository;
import com.jinsung.safety_road_inclass.domain.point.repository.UserPointsRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PointService - 포인트 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PointService {

    private static final String EDUCATION_COMPLETED_REASON = "\uAD50\uC721 \uC644\uB8CC";

    private final UserPointsRepository userPointsRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final UserRepository userRepository;

    /**
     * 포인트 잔액 조회
     */
    public PointsResponse getPoints(Long userId) {
        return userPointsRepository.findByUserId(userId)
                .map(PointsResponse::from)
                .orElse(PointsResponse.empty());
    }

    /**
     * 포인트 적립
     */
    @Transactional
    public AddPointsResponse addPoints(Long userId, int amount, String reason, String description) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.POINTS_INVALID_AMOUNT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (isDuplicateEducationReward(userId, reason, description)) {
            int currentBalance = userPointsRepository.findByUserId(userId)
                    .map(UserPoints::getBalance)
                    .orElse(0);
            log.info("Duplicate education reward ignored: userId={}, reason={}, description={}, balance={}",
                    userId, reason, description, currentBalance);
            return AddPointsResponse.builder()
                    .amount(0)
                    .balanceAfter(currentBalance)
                    .reason(reason)
                    .build();
        }

        UserPoints userPoints = userPointsRepository.findByUserId(userId)
                .orElseGet(() -> userPointsRepository.save(new UserPoints(user)));

        userPoints.earn(amount);

        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .amount(amount)
                .type(TransactionType.EARN)
                .reason(reason)
                .description(description)
                .balanceAfter(userPoints.getBalance())
                .build();
        pointTransactionRepository.save(transaction);

        log.info("Points earned: userId={}, amount={}, reason={}, balance={}", userId, amount, reason, userPoints.getBalance());

        return AddPointsResponse.builder()
                .amount(amount)
                .balanceAfter(userPoints.getBalance())
                .reason(reason)
                .build();
    }

    /**
     * 포인트 차감
     */
    @Transactional
    public AddPointsResponse spendPoints(Long userId, int amount, String reason, String description) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.POINTS_INVALID_AMOUNT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserPoints userPoints = userPointsRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINTS_INSUFFICIENT));

        if (userPoints.getBalance() < amount) {
            throw new CustomException(ErrorCode.POINTS_INSUFFICIENT);
        }

        userPoints.spend(amount);

        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .amount(amount)
                .type(TransactionType.SPEND)
                .reason(reason)
                .description(description)
                .balanceAfter(userPoints.getBalance())
                .build();
        pointTransactionRepository.save(transaction);

        log.info("Points spent: userId={}, amount={}, reason={}, balance={}", userId, amount, reason, userPoints.getBalance());

        return AddPointsResponse.builder()
                .amount(amount)
                .balanceAfter(userPoints.getBalance())
                .reason(reason)
                .build();
    }

    /**
     * 거래 내역 조회 (페이징)
     */
    public Page<PointTransactionResponse> getHistory(Long userId, Pageable pageable) {
        return pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(PointTransactionResponse::from);
    }

    private boolean isDuplicateEducationReward(Long userId, String reason, String description) {
        return EDUCATION_COMPLETED_REASON.equals(reason)
                && pointTransactionRepository.existsByUserIdAndTypeAndReasonAndDescription(
                        userId, TransactionType.EARN, reason, description);
    }
}
