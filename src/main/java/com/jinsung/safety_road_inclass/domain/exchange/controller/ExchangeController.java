package com.jinsung.safety_road_inclass.domain.exchange.controller;

import com.jinsung.safety_road_inclass.domain.exchange.dto.ExchangeRateResponse;
import com.jinsung.safety_road_inclass.domain.exchange.dto.ExchangeRequest;
import com.jinsung.safety_road_inclass.domain.exchange.dto.ExchangeResponse;
import com.jinsung.safety_road_inclass.domain.exchange.service.ExchangeService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * ExchangeController - 교환 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    /**
     * 포인트를 골드로 교환
     */
    @PostMapping("/points-to-gold")
    public ResponseEntity<ExchangeResponse> exchangePointsToGold(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody @Valid ExchangeRequest request) {

        ExchangeResponse response = exchangeService.exchangePointsToGold(
                principal.userId(),
                request.getPointsAmount());

        return ResponseEntity.ok(response);
    }

    /**
     * 교환 비율 조회
     */
    @GetMapping("/rate")
    public ResponseEntity<ExchangeRateResponse> getExchangeRate() {
        return ResponseEntity.ok(exchangeService.getExchangeRate());
    }
}
