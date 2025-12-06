package com.jinsung.safety_road_inclass.domain.review.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.checklist.entity.Checklist;
import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistStatus;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ReviewLog - 검토 로그 Entity
 */
@Entity
@Table(name = "review_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role reviewerRole;

    @Column(length = 1000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ChecklistStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ChecklistStatus newStatus;

    @Column(nullable = false)
    private LocalDateTime reviewedAt;

    @Builder
    public ReviewLog(Checklist checklist, User reviewer, ReviewAction action,
                     String comment, ChecklistStatus previousStatus) {
        this.checklist = checklist;
        this.reviewer = reviewer;
        this.action = action;
        this.reviewerRole = reviewer.getRole();
        this.comment = comment;
        this.previousStatus = previousStatus;
        this.reviewedAt = LocalDateTime.now();
        
        // 액션에 따른 새 상태 설정
        this.newStatus = (action == ReviewAction.APPROVE) 
            ? ChecklistStatus.APPROVED 
            : ChecklistStatus.REJECTED;
    }
}

