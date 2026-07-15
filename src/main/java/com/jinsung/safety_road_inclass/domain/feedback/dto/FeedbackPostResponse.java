package com.jinsung.safety_road_inclass.domain.feedback.dto;

import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackCategory;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackPost;
import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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

    // 공지 응답에만 포함되는 댓글 목록 (일반 의견 응답에서는 null)
    private List<FeedbackCommentResponse> comments;

    public static FeedbackPostResponse withComments(FeedbackPost post, List<FeedbackCommentResponse> comments) {
        FeedbackPostResponse response = from(post);
        response.comments = comments;
        return response;
    }

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
