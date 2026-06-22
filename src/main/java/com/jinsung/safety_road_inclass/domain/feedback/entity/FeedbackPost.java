package com.jinsung.safety_road_inclass.domain.feedback.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_posts", indexes = {
        @Index(name = "idx_feedback_author", columnList = "author_id"),
        @Index(name = "idx_feedback_status", columnList = "status"),
        @Index(name = "idx_feedback_category", columnList = "category"),
        @Index(name = "idx_feedback_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FeedbackCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FeedbackStatus status;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 120)
    private String pagePath;

    @Column(length = 500)
    private String attachmentUrl;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    private LocalDateTime repliedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_by_id")
    private User repliedBy;

    @Builder
    public FeedbackPost(User author, FeedbackCategory category, String title, String content, String pagePath,
                        String attachmentUrl) {
        this.author = author;
        this.category = category != null ? category : FeedbackCategory.OTHER;
        this.status = FeedbackStatus.OPEN;
        this.title = title;
        this.content = content;
        this.pagePath = pagePath;
        this.attachmentUrl = attachmentUrl;
    }

    public void updateStatus(FeedbackStatus status) {
        this.status = status;
    }

    public void reply(String adminReply, User repliedBy) {
        this.adminReply = adminReply;
        this.repliedBy = repliedBy;
        this.repliedAt = LocalDateTime.now();
        this.status = FeedbackStatus.ANSWERED;
    }
}
