package com.jinsung.safety_road_inclass.domain.team.controller;

import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.domain.team.dto.MembershipResponse;
import com.jinsung.safety_road_inclass.domain.team.dto.PendingMemberResponse;
import com.jinsung.safety_road_inclass.domain.team.dto.TeamCreateRequest;
import com.jinsung.safety_road_inclass.domain.team.dto.TeamMemberResponse;
import com.jinsung.safety_road_inclass.domain.team.dto.TeamSummaryResponse;
import com.jinsung.safety_road_inclass.domain.team.service.TeamMembershipService;
import com.jinsung.safety_road_inclass.domain.team.service.TeamService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Team", description = "팀 검색·생성 API (B2)")
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TeamMembershipService membershipService;

    @Operation(summary = "팀 검색", description = "현장 + 부분 일치 자동완성 (최대 20건)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamSummaryResponse>>> search(
            @RequestParam("siteName") String siteName,
            @RequestParam(value = "q", required = false) String query) {
        List<TeamSummaryResponse> teams = teamService.search(siteName, query);
        return ResponseEntity.ok(ApiResponse.success(teams));
    }

    @Operation(summary = "팀 생성", description = "동일 현장 내 정규화 이름 중복 시 409. 생성자가 ACTIVE 리더가 됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<TeamSummaryResponse>> create(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody TeamCreateRequest request) {
        TeamSummaryResponse response = teamService.create(principal.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 팀 가입 상태 조회", description = "ACTIVE/PENDING/LEFT 중 현재 유효한 가입 상태를 반환합니다.")
    @GetMapping("/me/membership")
    public ResponseEntity<ApiResponse<MembershipResponse>> myMembership(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.getCurrentMembership(principal.userId())));
    }

    @Operation(summary = "팀 가입 신청", description = "PENDING 상태로 신청. ACTIVE 멤버십 보유 시 409.")
    @PostMapping("/{teamId}/join")
    public ResponseEntity<ApiResponse<MembershipResponse>> join(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.requestJoin(teamId, principal.userId())));
    }

    @Operation(summary = "신청자 목록 조회 (리더 전용)")
    @GetMapping("/{teamId}/pending")
    public ResponseEntity<ApiResponse<List<PendingMemberResponse>>> listPending(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.listPending(teamId, principal.userId())));
    }

    @Operation(summary = "ACTIVE 멤버 목록 조회")
    @GetMapping("/{teamId}/members")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> listMembers(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.listActiveMembers(teamId, principal.userId())));
    }

    @Operation(summary = "가입 승인 (리더 전용)")
    @PostMapping("/{teamId}/members/{userId}/approve")
    public ResponseEntity<ApiResponse<MembershipResponse>> approve(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                membershipService.approve(teamId, userId, principal.userId())));
    }

    @Operation(summary = "가입 거절 (리더 전용) — PENDING 멤버십 삭제")
    @PostMapping("/{teamId}/members/{userId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        membershipService.reject(teamId, userId, principal.userId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "팀 탈퇴 (본인)")
    @PostMapping("/{teamId}/leave")
    public ResponseEntity<ApiResponse<Void>> leave(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long teamId) {
        membershipService.leave(teamId, principal.userId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "멤버 강퇴 (리더 전용)")
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> kick(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        membershipService.kick(teamId, userId, principal.userId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
