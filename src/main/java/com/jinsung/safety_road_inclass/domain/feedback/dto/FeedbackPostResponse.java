package com.jinsung.safety_road_inclass.domain.feedback.dto;

import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackCategory;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackPost;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FeedbackPostResponse {

    private Long id;
    private FeedbackCategory category;
    private FeedbackStatus status;
    private String title;
    private String content;
    private String pagePath;
    private String attachmentUrl;
    private String authorName;
    private String authorUsername;
    private String adminReply;
    private String repliedByName;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FeedbackPostResponse from(FeedbackPost post) {
        return FeedbackPostResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .status(post.getStatus())
                .title(post.getTitle())
                .content(post.getContent())
                .pagePath(post.getPagePath())
                .attachmentUrl(post.getAttachmentUrl())
                .authorName(post.getAuthor().getName())
                .authorUsername(post.getAuthor().getUsername())
                .adminReply(post.getAdminReply())
                .repliedByName(post.getRepliedBy() != null ? post.getRepliedBy().getName() : null)
                .repliedAt(post.getRepliedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
