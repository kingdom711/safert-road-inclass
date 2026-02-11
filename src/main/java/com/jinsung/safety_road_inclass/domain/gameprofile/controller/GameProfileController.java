package com.jinsung.safety_road_inclass.domain.gameprofile.controller;

import com.jinsung.safety_road_inclass.domain.gameprofile.dto.*;
import com.jinsung.safety_road_inclass.domain.gameprofile.service.GameProfileService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GameProfileController - 게임 프로필 API
 */
@Tag(name = "GameProfile", description = "게임 프로필 관리 API (레벨, 역할, 전직)")
@RestController
@RequestMapping("/api/v1/game-profile/me")
@RequiredArgsConstructor
public class GameProfileController {

    private final GameProfileService gameProfileService;

    @Operation(summary = "전체 게임 데이터 조회", description = "로그인 시 호출하여 전체 게임 데이터(프로필+전직+포인트+스트릭)를 한 번에 조회합니다. 크로스 디바이스 동기화용.")
    @GetMapping("/full")
    public ApiResponse<FullGameDataResponse> getFullGameData(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.success(gameProfileService.getFullGameData(principal.userId()));
    }

    @Operation(summary = "게임 프로필 조회", description = "현재 로그인한 유저의 게임 프로필을 조회합니다.")
    @GetMapping
    public ApiResponse<GameProfileResponse> getProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.success(gameProfileService.getOrCreateProfile(principal.userId()));
    }

    @Operation(summary = "게임 프로필 업데이트", description = "게임 역할, 활성 전직 등을 업데이트합니다.")
    @PutMapping
    public ApiResponse<GameProfileResponse> updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody GameProfileUpdateRequest request) {
        return ApiResponse.success(
                gameProfileService.updateProfile(principal.userId(), request.getGameRole(),
                        request.getActiveSpecialization()));
    }

    @Operation(summary = "경험치 추가", description = "현재 로그인한 유저에게 경험치를 추가합니다.")
    @PostMapping("/exp")
    public ApiResponse<AddExpResponse> addExp(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AddExpRequest request) {
        return ApiResponse.success(
                gameProfileService.addExp(principal.userId(), request.getAmount(), request.getSource()));
    }

    @Operation(summary = "게임 역할 설정", description = "기술인/관리감독자/안전관리자 등 게임 내 역할을 설정합니다.")
    @PutMapping("/role")
    public ApiResponse<GameProfileResponse> setRole(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody SetRoleRequest request) {
        return ApiResponse.success(gameProfileService.setGameRole(principal.userId(), request.getRole()));
    }

    @Operation(summary = "해금된 전직 목록 조회", description = "유저가 해금한 전직 목록을 조회합니다.")
    @GetMapping("/specializations")
    public ApiResponse<List<SpecializationResponse>> getSpecializations(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.success(gameProfileService.getSpecializations(principal.userId()));
    }

    @Operation(summary = "전직 해금", description = "새로운 전직을 해금합니다.")
    @PostMapping("/specializations")
    public ApiResponse<SpecializationResponse> unlockSpecialization(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UnlockSpecializationRequest request) {
        return ApiResponse.success(
                gameProfileService.unlockSpecialization(principal.userId(), request.getSpecId(),
                        request.getEducationProgress()));
    }

    @Operation(summary = "활성 전직 변경", description = "해금된 전직 중 하나를 활성 전직으로 설정합니다.")
    @PutMapping("/specializations/active")
    public ApiResponse<GameProfileResponse> setActiveSpecialization(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody SetActiveSpecializationRequest request) {
        return ApiResponse.success(
                gameProfileService.setActiveSpecialization(principal.userId(), request.getSpecId()));
    }

    @Operation(summary = "localStorage 데이터 동기화", description = "프론트엔드 localStorage에 저장된 게임 데이터를 서버로 마이그레이션합니다.")
    @PostMapping("/sync")
    public ApiResponse<FullGameDataResponse> syncFromLocalStorage(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody GameProfileSyncRequest request) {
        return ApiResponse.success(
                gameProfileService.syncFromLocalStorage(principal.userId(), request));
    }
}
