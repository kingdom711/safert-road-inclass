package com.jinsung.safety_road_inclass.domain.gold.dto;

import com.jinsung.safety_road_inclass.domain.gold.entity.GoldTransaction;
import com.jinsung.safety_road_inclass.domain.gold.entity.GoldTransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * GoldTransactionResponse - 골드 거래 내역 응답 DTO
 */
@Getter
@Builder
public class GoldTransactionResponse {

    private Long id;
    private int amount;
    private GoldTransactionType type;
    private String reason;
    private String description;
    private int balanceAfter;
    private LocalDateTime createdAt;

    public static GoldTransactionResponse from(GoldTransaction transaction) {
        return GoldTransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .reason(transaction.getReason())
                .description(transaction.getDescription())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
