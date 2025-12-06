package com.jinsung.safety_road_inclass.domain.review.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.checklist.entity.Checklist;
import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistStatus;
import com.jinsung.safety_road_inclass.domain.checklist.repository.ChecklistRepository;
import com.jinsung.safety_road_inclass.domain.review.dto.ReviewLogResponse;
import com.jinsung.safety_road_inclass.domain.review.dto.ReviewRequest;
import com.jinsung.safety_road_inclass.domain.review.entity.ReviewAction;
import com.jinsung.safety_road_inclass.domain.review.entity.ReviewLog;
import com.jinsung.safety_road_inclass.domain.review.repository.ReviewLogRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ReviewService - 검토 및 승인 비즈니스 로직
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewLogRepository reviewLogRepository;
    private final ChecklistRepository checklistRepository;

    /**
     * 체크리스트 검토 (승인/반려)
     */
    @Transactional
    public ReviewLogResponse review(Long checklistId, ReviewRequest request, User reviewer) {
        // 1. 체크리스트 조회
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHECKLIST_NOT_FOUND));

        // 2. 검토 가능 상태 확인 (SUBMITTED만 검토 가능)
        if (checklist.getStatus() != ChecklistStatus.SUBMITTED) {
            throw new CustomException(ErrorCode.INVALID_CHECKLIST_STATUS);
        }

        // 3. 본인이 작성한 체크리스트는 검토 불가
        if (checklist.getCreatedBy().getId().equals(reviewer.getId())) {
            throw new CustomException(ErrorCode.CANNOT_REVIEW_OWN_CHECKLIST);
        }

        // 4. 권한 확인 (SUPERVISOR, SAFETY_MANAGER만 검토 가능)
        if (reviewer.getRole() != Role.ROLE_SUPERVISOR 
                && reviewer.getRole() != Role.ROLE_SAFETY_MANAGER) {
            throw new CustomException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        // 5. 이전 상태 저장
        ChecklistStatus previousStatus = checklist.getStatus();

        // 6. 검토 로그 생성
        ReviewLog reviewLog = ReviewLog.builder()
                .checklist(checklist)
                .reviewer(reviewer)
                .action(request.getAction())
                .comment(request.getComment())
                .previousStatus(previousStatus)
                .build();

        // 7. 체크리스트 상태 변경
        if (request.getAction() == ReviewAction.APPROVE) {
            checklist.approve();
            log.info("체크리스트 승인: checklistId={}, reviewerId={}", checklistId, reviewer.getId());
        } else {
            checklist.reject();
            log.info("체크리스트 반려: checklistId={}, reviewerId={}, comment={}", 
                     checklistId, reviewer.getId(), request.getComment());
        }

        ReviewLog saved = reviewLogRepository.save(reviewLog);

        return ReviewLogResponse.from(saved);
    }

    /**
     * 체크리스트별 검토 이력 조회
     */
    public List<ReviewLogResponse> getChecklistReviewHistory(Long checklistId) {
        return reviewLogRepository.findByChecklistIdOrderByReviewedAtDesc(checklistId).stream()
                .map(ReviewLogResponse::from)
                .toList();
    }

    /**
     * 최근 검토 이력 조회
     */
    public List<ReviewLogResponse> getRecentReviews() {
        return reviewLogRepository.findRecentReviews().stream()
                .map(ReviewLogResponse::from)
                .toList();
    }
}

