package com.jinsung.safety_road_inclass.domain.feedback.controller;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.service.AuthService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.domain.feedback.dto.CommentCreateRequest;
import com.jinsung.safety_road_inclass.domain.feedback.dto.FeedbackCommentResponse;
import com.jinsung.safety_road_inclass.domain.feedback.dto.FeedbackCreateRequest;
import com.jinsung.safety_road_inclass.domain.feedback.dto.FeedbackPostResponse;
import com.jinsung.safety_road_inclass.domain.feedback.service.FeedbackService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final AuthService authService;

    @PostMapping
    public ApiResponse<FeedbackPostResponse> create(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody FeedbackCreateRequest request) {
        User currentUser = authService.getUserById(principal.userId());
        return ApiResponse.success(feedbackService.create(request, currentUser));
    }

    @GetMapping("/me")
    public ApiResponse<List<FeedbackPostResponse>> getMine(@AuthenticationPrincipal CustomUserPrincipal principal) {
        User currentUser = authService.getUserById(principal.userId());
        return ApiResponse.success(feedbackService.getMine(currentUser));
    }

    @GetMapping("/notices")
    public ApiResponse<List<FeedbackPostResponse>> getNotices() {
        return ApiResponse.success(feedbackService.getNotices());
    }

    @PostMapping("/notices/{postId}/comments")
    public ApiResponse<FeedbackCommentResponse> addNoticeComment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        User currentUser = authService.getUserById(principal.userId());
        return ApiResponse.success(feedbackService.addNoticeComment(postId, request.getContent(), currentUser));
    }

    @DeleteMapping("/notices/{postId}/comments/{commentId}")
    public ApiResponse<Void> deleteNoticeComment(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        User currentUser = authService.getUserById(principal.userId());
        feedbackService.deleteNoticeComment(postId, commentId, currentUser);
        return ApiResponse.success();
    }
}
