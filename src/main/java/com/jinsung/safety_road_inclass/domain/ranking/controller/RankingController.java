package com.jinsung.safety_road_inclass.domain.ranking.controller;

import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.domain.ranking.dto.MyRankResponse;
import com.jinsung.safety_road_inclass.domain.ranking.dto.RankingEntryResponse;
import com.jinsung.safety_road_inclass.domain.ranking.dto.TeamRankingResponse;
import com.jinsung.safety_road_inclass.domain.ranking.service.RankingService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RankingController - 랭킹 API
 *
 * 프론트엔드 API 경로:
 * - GET /api/v1/users/rankings       → 개인 랭킹
 * - GET /api/v1/users/me/rank        → 내 랭킹
 * - GET /api/v1/teams/rankings       → 팀(역할 그룹) 랭킹
 */
@Tag(name = "Ranking", description = "랭킹 조회 API")
@RestController
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "개인 랭킹 조회", description = "포인트/레벨/스트릭 기준 전체 사용자 랭킹을 조회합니다.")
    @GetMapping("/api/v1/users/rankings")
    public ApiResponse<List<RankingEntryResponse>> getRankings(
            @RequestParam(defaultValue = "points") String type,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "all") String period,
            @RequestParam(required = false) String role) {
        List<RankingEntryResponse> rankings = rankingService.getRankings(type, limit, role);
        return ApiResponse.success(rankings);
    }

    @Operation(summary = "내 랭킹 조회", description = "현재 로그인한 사용자의 순위를 조회합니다.")
    @GetMapping("/api/v1/users/me/rank")
    public ApiResponse<MyRankResponse> getMyRank(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "points") String type) {
        MyRankResponse myRank = rankingService.getMyRank(principal.userId(), type);
        return ApiResponse.success(myRank);
    }

    @Operation(summary = "팀 랭킹 조회", description = "역할 그룹 기준 팀 랭킹을 조회합니다.")
    @GetMapping("/api/v1/teams/rankings")
    public ApiResponse<List<TeamRankingResponse>> getTeamRankings(
            @RequestParam(defaultValue = "points") String type,
            @RequestParam(defaultValue = "10") int limit) {
        List<TeamRankingResponse> teamRankings = rankingService.getTeamRankings(type, limit);
        return ApiResponse.success(teamRankings);
    }
}
