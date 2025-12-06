package com.jinsung.safety_road_inclass.domain.review.dto;

import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistStatus;
import com.jinsung.safety_road_inclass.domain.review.entity.ReviewAction;
import com.jinsung.safety_road_inclass.domain.review.entity.ReviewLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * ReviewLogResponse - 검토 로그 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "검토 로그 응답")
public class ReviewLogResponse {

    @Schema(description = "로그 ID", example = "1")
    private Long id;

    @Schema(description = "체크리스트 ID", example = "1")
    private Long checklistId;

    @Schema(description = "검토자 이름", example = "이관리")
    private String reviewerName;

    @Schema(description = "검토자 역할", example = "SUPERVISOR")
    private String reviewerRole;

    @Schema(description = "검토 액션", example = "APPROVE")
    private ReviewAction action;

    @Schema(description = "검토 의견", example = "검토 완료, 문제 없음")
    private String comment;

    @Schema(description = "이전 상태", example = "SUBMITTED")
    private ChecklistStatus previousStatus;

    @Schema(description = "변경된 상태", example = "APPROVED")
    private ChecklistStatus newStatus;

    @Schema(description = "검토 시간", example = "2025-12-06T14:30:00")
    private LocalDateTime reviewedAt;

    public static ReviewLogResponse from(ReviewLog log) {
        return ReviewLogResponse.builder()
                .id(log.getId())
                .checklistId(log.getChecklist().getId())
                .reviewerName(log.getReviewer().getName())
                .reviewerRole(log.getReviewerRole().name())
                .action(log.getAction())
                .comment(log.getComment())
                .previousStatus(log.getPreviousStatus())
                .newStatus(log.getNewStatus())
                .reviewedAt(log.getReviewedAt())
                .build();
    }
}

