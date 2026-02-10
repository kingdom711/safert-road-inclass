package com.jinsung.safety_road_inclass.domain.point.dto;

import com.jinsung.safety_road_inclass.domain.point.entity.PointTransaction;
import com.jinsung.safety_road_inclass.domain.point.entity.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * PointTransactionResponse - 포인트 거래 내역 응답 DTO
 */
@Getter
@Builder
public class PointTransactionResponse {

    private Long id;
    private int amount;
    private TransactionType type;
    private String reason;
    private String description;
    private int balanceAfter;
    private LocalDateTime createdAt;

    public static PointTransactionResponse from(PointTransaction transaction) {
        return PointTransactionResponse.builder()
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
