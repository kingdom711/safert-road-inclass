package com.jinsung.safety_road_inclass.domain.feedback.dto;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FeedbackCommentResponse {

    private Long id;
    private String content;
    private String authorName;
    private String authorUsername;
    private boolean authorIsAdmin;
    private LocalDateTime createdAt;

    public static FeedbackCommentResponse from(FeedbackComment comment) {
        Role role = comment.getAuthor().getRole();
        return FeedbackCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorName(comment.getAuthor().getName())
                .authorUsername(comment.getAuthor().getUsername())
                .authorIsAdmin(role == Role.ROLE_ADMIN || role == Role.ROLE_PROJECT_ADMIN)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
