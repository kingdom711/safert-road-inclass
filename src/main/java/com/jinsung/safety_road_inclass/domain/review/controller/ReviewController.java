package com.jinsung.safety_road_inclass.domain.review.controller;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.review.dto.ReviewLogResponse;
import com.jinsung.safety_road_inclass.domain.review.dto.ReviewRequest;
import com.jinsung.safety_road_inclass.domain.review.service.ReviewService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ReviewController - 검토 및 승인 REST API
 */
@Tag(name = "Review", description = "체크리스트 검토 및 승인 API")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "체크리스트 검토", description = "체크리스트를 승인 또는 반려합니다. (SUPERVISOR 이상 권한 필요)")
    @PostMapping("/{checklistId}")
    public ApiResponse<ReviewLogResponse> reviewChecklist(
            @Parameter(hidden = true) @AuthenticationPrincipal User reviewer,
            @PathVariable Long checklistId,
            @Valid @RequestBody ReviewRequest request) {
        
        log.info("체크리스트 검토 요청: checklistId={}, action={}, reviewerId={}", 
                 checklistId, request.getAction(), reviewer.getId());
        
        ReviewLogResponse response = reviewService.review(checklistId, request, reviewer);
        
        return ApiResponse.success(response);
    }

    @Operation(summary = "체크리스트 검토 이력 조회", description = "특정 체크리스트의 검토 이력을 조회합니다.")
    @GetMapping("/{checklistId}/history")
    public ApiResponse<List<ReviewLogResponse>> getChecklistReviewHistory(
            @PathVariable Long checklistId) {
        
        List<ReviewLogResponse> history = reviewService.getChecklistReviewHistory(checklistId);
        
        return ApiResponse.success(history);
    }

    @Operation(summary = "최근 검토 이력 조회", description = "최근 검토 이력을 조회합니다.")
    @GetMapping("/recent")
    public ApiResponse<List<ReviewLogResponse>> getRecentReviews() {
        List<ReviewLogResponse> reviews = reviewService.getRecentReviews();
        return ApiResponse.success(reviews);
    }
}

