package com.jinsung.safety_road_inclass.domain.feedback.controller;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.service.AuthService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.domain.feedback.dto.FeedbackPostResponse;
import com.jinsung.safety_road_inclass.domain.feedback.dto.FeedbackReplyRequest;
import com.jinsung.safety_road_inclass.domain.feedback.dto.FeedbackStatusUpdateRequest;
import com.jinsung.safety_road_inclass.domain.feedback.dto.NoticeCreateRequest;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackStatus;
import com.jinsung.safety_road_inclass.domain.feedback.service.FeedbackService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final FeedbackService feedbackService;
    private final AuthService authService;

    @GetMapping
    public ApiResponse<List<FeedbackPostResponse>> getAll(@RequestParam(required = false) FeedbackStatus status) {
        return ApiResponse.success(feedbackService.getAll(status));
    }

    @PutMapping("/{postId}/status")
    public ApiResponse<FeedbackPostResponse> updateStatus(
            @PathVariable Long postId,
            @Valid @RequestBody FeedbackStatusUpdateRequest request) {
        return ApiResponse.success(feedbackService.updateStatus(postId, request.getStatus()));
    }

    @PostMapping("/{postId}/reply")
    public ApiResponse<FeedbackPostResponse> reply(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long postId,
            @Valid @RequestBody FeedbackReplyRequest request) {
        User currentUser = authService.getUserById(principal.userId());
        return ApiResponse.success(feedbackService.reply(postId, request.getReply(), currentUser));
    }

    @PostMapping("/notice")
    public ApiResponse<FeedbackPostResponse> createNotice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody NoticeCreateRequest request) {
        User currentUser = authService.getUserById(principal.userId());
        return ApiResponse.success(feedbackService.createNotice(request, currentUser));
    }

    @DeleteMapping("/notice/{postId}")
    public ApiResponse<Void> deleteNotice(@PathVariable Long postId) {
        feedbackService.deleteNotice(postId);
        return ApiResponse.success();
    }
}
