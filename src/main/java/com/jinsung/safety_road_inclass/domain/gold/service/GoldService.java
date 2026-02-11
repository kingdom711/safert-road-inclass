package com.jinsung.safety_road_inclass.domain.gold.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.gold.dto.AddGoldResponse;
import com.jinsung.safety_road_inclass.domain.gold.dto.GoldResponse;
import com.jinsung.safety_road_inclass.domain.gold.dto.GoldTransactionResponse;
import com.jinsung.safety_road_inclass.domain.gold.entity.GoldTransaction;
import com.jinsung.safety_road_inclass.domain.gold.entity.GoldTransactionType;
import com.jinsung.safety_road_inclass.domain.gold.entity.UserGold;
import com.jinsung.safety_road_inclass.domain.gold.repository.GoldTransactionRepository;
import com.jinsung.safety_road_inclass.domain.gold.repository.UserGoldRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * GoldService - 골드 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GoldService {

    private final UserGoldRepository userGoldRepository;
    private final GoldTransactionRepository goldTransactionRepository;
    private final UserRepository userRepository;

    /**
     * 골드 잔액 조회
     */
    public GoldResponse getGold(Long userId) {
        return userGoldRepository.findByUserId(userId)
                .map(GoldResponse::from)
                .orElse(GoldResponse.empty());
    }

    /**
     * 골드 적립
     */
    @Transactional
    public AddGoldResponse addGold(Long userId, int amount, String reason, String description) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.GOLD_INVALID_AMOUNT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserGold userGold = userGoldRepository.findByUserId(userId)
                .orElseGet(() -> userGoldRepository.save(new UserGold(user)));

        userGold.earn(amount);

        GoldTransaction transaction = GoldTransaction.builder()
                .user(user)
                .amount(amount)
                .type(GoldTransactionType.EARN)
                .reason(reason)
                .description(description)
                .balanceAfter(userGold.getBalance())
                .build();
        goldTransactionRepository.save(transaction);

        log.info("Gold earned: userId={}, amount={}, reason={}, balance={}", userId, amount, reason,
                userGold.getBalance());

        return AddGoldResponse.builder()
                .amount(amount)
                .balanceAfter(userGold.getBalance())
                .reason(reason)
                .build();
    }

    /**
     * 골드 차감
     */
    @Transactional
    public AddGoldResponse spendGold(Long userId, int amount, String reason, String description) {
        if (amount <= 0) {
            throw new CustomException(ErrorCode.GOLD_INVALID_AMOUNT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserGold userGold = userGoldRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.GOLD_INSUFFICIENT));

        if (userGold.getBalance() < amount) {
            throw new CustomException(ErrorCode.GOLD_INSUFFICIENT);
        }

        userGold.spend(amount);

        GoldTransaction transaction = GoldTransaction.builder()
                .user(user)
                .amount(amount)
                .type(GoldTransactionType.SPEND)
                .reason(reason)
                .description(description)
                .balanceAfter(userGold.getBalance())
                .build();
        goldTransactionRepository.save(transaction);

        log.info("Gold spent: userId={}, amount={}, reason={}, balance={}", userId, amount, reason,
                userGold.getBalance());

        return AddGoldResponse.builder()
                .amount(amount)
                .balanceAfter(userGold.getBalance())
                .reason(reason)
                .build();
    }

    /**
     * 거래 내역 조회 (페이징)
     */
    public Page<GoldTransactionResponse> getHistory(Long userId, Pageable pageable) {
        return goldTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(GoldTransactionResponse::from);
    }
}
