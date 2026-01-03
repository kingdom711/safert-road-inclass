package com.jinsung.safety_road_inclass.domain.risk.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistItem;
import com.jinsung.safety_road_inclass.domain.checklist.repository.ChecklistItemRepository;
import com.jinsung.safety_road_inclass.domain.risk.dto.*;
import com.jinsung.safety_road_inclass.domain.risk.entity.Countermeasure;
import com.jinsung.safety_road_inclass.domain.risk.entity.RiskAssessment;
import com.jinsung.safety_road_inclass.domain.risk.entity.RiskLevel;
import com.jinsung.safety_road_inclass.domain.risk.repository.CountermeasureRepository;
import com.jinsung.safety_road_inclass.domain.risk.repository.RiskAssessmentRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * RiskService - 위험성 평가 비즈니스 로직
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final CountermeasureRepository countermeasureRepository;

    /**
     * 위험성 평가 대상 목록 조회 (riskFlag == true && 미평가)
     */
    public List<PendingRiskResponse> getPendingRisks() {
        List<ChecklistItem> pendingItems = checklistItemRepository
                .findByRiskFlagTrueAndRiskAssessmentIsNull();
        
        return pendingItems.stream()
                .map(PendingRiskResponse::from)
                .toList();
    }

    /**
     * 위험성 평가 등록
     */
    @Transactional
    public RiskAssessmentResponse assess(Long checklistItemId, 
                                         RiskAssessRequest request,
                                         User currentUser) {
        // 1. 대상 항목 조회
        ChecklistItem item = checklistItemRepository.findById(checklistItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHECKLIST_ITEM_NOT_FOUND));

        // 2. 이미 평가된 항목인지 검증
        if (riskAssessmentRepository.existsByChecklistItemId(checklistItemId)) {
            throw new CustomException(ErrorCode.ALREADY_ASSESSED);
        }

        // 3. 위험 플래그 검증
        if (!item.getRiskFlag()) {
            throw new CustomException(ErrorCode.NOT_RISK_ITEM);
        }

        // 4. 평가 생성 (점수/레벨 자동 계산)
        RiskAssessment assessment = RiskAssessment.builder()
                .checklistItem(item)
                .assessedBy(currentUser)
                .frequency(request.getFrequency())
                .severity(request.getSeverity())
                .description(request.getDescription())
                .build();

        // 5. 대책 추가
        if (request.getCountermeasures() != null && !request.getCountermeasures().isEmpty()) {
            for (CountermeasureRequest cmReq : request.getCountermeasures()) {
                Countermeasure countermeasure = Countermeasure.builder()
                        .createdBy(currentUser)
                        .content(cmReq.getContent())
                        .dueDate(cmReq.getDueDate())
                        .build();
                assessment.addCountermeasure(countermeasure);
            }
        }

        RiskAssessment saved = riskAssessmentRepository.save(assessment);

        log.info("위험성 평가 완료: itemId={}, score={}, level={}", 
                 checklistItemId, saved.getRiskScore(), saved.getRiskLevel());

        return RiskAssessmentResponse.from(saved);
    }

    /**
     * 위험성 평가 상세 조회
     */
    public RiskAssessmentResponse getAssessmentDetail(Long assessmentId) {
        RiskAssessment assessment = riskAssessmentRepository.findByIdWithDetails(assessmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.RISK_ASSESSMENT_NOT_FOUND));

        return RiskAssessmentResponse.from(assessment);
    }

    /**
     * 고위험 항목 조회 (HIGH, CRITICAL)
     */
    public List<RiskAssessmentResponse> getHighRiskItems() {
        return riskAssessmentRepository.findHighRiskItems().stream()
                .map(RiskAssessmentResponse::from)
                .toList();
    }

    /**
     * 위험 레벨별 조회
     */
    public List<RiskAssessmentResponse> getAssessmentsByLevel(RiskLevel level) {
        return riskAssessmentRepository.findByRiskLevelOrderByAssessedAtDesc(level).stream()
                .map(RiskAssessmentResponse::from)
                .toList();
    }

    /**
     * 미완료 대책 목록 조회
     */
    public List<CountermeasureResponse> getIncompleteCountermeasures() {
        return countermeasureRepository.findIncomplete().stream()
                .map(CountermeasureResponse::from)
                .toList();
    }

    /**
     * 기한 초과 대책 조회
     */
    public List<CountermeasureResponse> getOverdueCountermeasures() {
        LocalDate today = LocalDate.now();
        return countermeasureRepository.findOverdue(today).stream()
                .map(CountermeasureResponse::from)
                .toList();
    }

    /**
     * 대책 완료 처리
     */
    @Transactional
    public CountermeasureResponse completeCountermeasure(Long countermeasureId, User currentUser) {
        Countermeasure countermeasure = countermeasureRepository.findById(countermeasureId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        countermeasure.complete();

        log.info("대책 완료 처리: countermeasureId={}, userId={}", countermeasureId, currentUser.getId());

        return CountermeasureResponse.from(countermeasure);
    }
}

