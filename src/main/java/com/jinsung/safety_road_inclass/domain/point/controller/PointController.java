package com.jinsung.safety_road_inclass.domain.point.controller;

import com.jinsung.safety_road_inclass.domain.point.dto.AddPointsRequest;
import com.jinsung.safety_road_inclass.domain.point.dto.AddPointsResponse;
import com.jinsung.safety_road_inclass.domain.point.dto.PointTransactionResponse;
import com.jinsung.safety_road_inclass.domain.point.dto.PointsResponse;
import com.jinsung.safety_road_inclass.domain.point.service.PointService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * PointController - 포인트 API
 */
@Tag(name = "Point", description = "포인트 관리 API")
@RestController
@RequestMapping("/api/v1/users/me/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 잔액 조회", description = "현재 로그인한 유저의 포인트 잔액을 조회합니다.")
    @GetMapping
    public ApiResponse<PointsResponse> getPoints(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.success(pointService.getPoints(principal.userId()));
    }

    @Operation(summary = "포인트 적립", description = "현재 로그인한 유저에게 포인트를 적립합니다.")
    @PostMapping("/add")
    public ApiResponse<AddPointsResponse> addPoints(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AddPointsRequest request) {
        return ApiResponse.success(
                pointService.addPoints(principal.userId(), request.getAmount(), request.getReason(), request.getDescription()));
    }

    @Operation(summary = "포인트 차감", description = "현재 로그인한 유저의 포인트를 차감합니다.")
    @PostMapping("/spend")
    public ApiResponse<AddPointsResponse> spendPoints(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AddPointsRequest request) {
        return ApiResponse.success(
                pointService.spendPoints(principal.userId(), request.getAmount(), request.getReason(), request.getDescription()));
    }

    @Operation(summary = "거래 내역 조회", description = "현재 로그인한 유저의 포인트 거래 내역을 페이징 조회합니다.")
    @GetMapping("/history")
    public ApiResponse<Page<PointTransactionResponse>> getHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(pointService.getHistory(principal.userId(), pageable));
    }
}
