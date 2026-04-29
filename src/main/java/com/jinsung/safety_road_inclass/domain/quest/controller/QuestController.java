package com.jinsung.safety_road_inclass.domain.quest.controller;

import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.domain.quest.dto.ClaimTeamQuestRewardResponse;
import com.jinsung.safety_road_inclass.domain.quest.dto.TeamQuestListResponse;
import com.jinsung.safety_road_inclass.domain.quest.dto.TeamQuestResponse;
import com.jinsung.safety_road_inclass.domain.quest.dto.TeamQuestSummaryResponse;
import com.jinsung.safety_road_inclass.domain.quest.service.QuestQueryService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Quest", description = "팀 퀘스트 API")
@RestController
@RequestMapping("/api/v1/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestQueryService questQueryService;

    @GetMapping("/team")
    public ResponseEntity<ApiResponse<List<TeamQuestResponse>>> getTeamQuests(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(value = "period", required = false, defaultValue = "all") String period) {
        return ResponseEntity.ok(ApiResponse.success(
                questQueryService.getCurrentTeamQuests(principal.userId(), period)));
    }

    @GetMapping("/team/me")
    public ResponseEntity<ApiResponse<TeamQuestListResponse>> getMyTeamQuests(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(value = "period", required = false, defaultValue = "all") String period) {
        return ResponseEntity.ok(ApiResponse.success(
                questQueryService.getCurrentTeamQuestList(principal.userId(), period)));
    }

    @GetMapping("/team/me/summary")
    public ResponseEntity<ApiResponse<TeamQuestSummaryResponse>> getMyTeamQuestSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                questQueryService.getCurrentTeamQuestSummary(principal.userId())));
    }

    @PostMapping("/team/{questId}/claim")
    public ResponseEntity<ApiResponse<ClaimTeamQuestRewardResponse>> claimTeamQuestReward(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long questId) {
        return ResponseEntity.ok(ApiResponse.success(
                questQueryService.claimTeamQuestReward(principal.userId(), questId)));
    }
}
