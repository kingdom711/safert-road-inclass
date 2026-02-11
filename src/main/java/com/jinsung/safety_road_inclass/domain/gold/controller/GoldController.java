package com.jinsung.safety_road_inclass.domain.gold.controller;

import com.jinsung.safety_road_inclass.domain.gold.dto.GoldResponse;
import com.jinsung.safety_road_inclass.domain.gold.dto.GoldTransactionResponse;
import com.jinsung.safety_road_inclass.domain.gold.service.GoldService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GoldController - 골드 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/gold")
@RequiredArgsConstructor
public class GoldController {

    private final GoldService goldService;

    /**
     * 골드 잔액 조회
     */
    @GetMapping("/balance")
    public ResponseEntity<GoldResponse> getBalance(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(
                goldService.getGold(principal.userId()));
    }

    /**
     * 골드 거래 내역 조회
     */
    @GetMapping("/history")
    public ResponseEntity<Page<GoldTransactionResponse>> getHistory(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Pageable pageable) {
        return ResponseEntity.ok(
                goldService.getHistory(principal.userId(), pageable));
    }
}
