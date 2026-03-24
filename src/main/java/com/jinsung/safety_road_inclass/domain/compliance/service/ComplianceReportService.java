package com.jinsung.safety_road_inclass.domain.compliance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.checklist.repository.ChecklistRepository;
import com.jinsung.safety_road_inclass.domain.compliance.dto.ComplianceReportResponse;
import com.jinsung.safety_road_inclass.domain.compliance.entity.ComplianceReport;
import com.jinsung.safety_road_inclass.domain.compliance.repository.ComplianceReportRepository;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationCompletionRepository;
import com.jinsung.safety_road_inclass.domain.risk.entity.RiskLevel;
import com.jinsung.safety_road_inclass.domain.risk.repository.CountermeasureRepository;
import com.jinsung.safety_road_inclass.domain.risk.repository.RiskAssessmentRepository;
import com.jinsung.safety_road_inclass.domain.workstop.entity.ReportStatus;
import com.jinsung.safety_road_inclass.domain.workstop.repository.WorkStopReportRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ComplianceReportService - 컴플라이언스 리포트 생성·조회 서비스
 *
 * 중대재해처벌법 제4조 안전보건관리체계 의무 이행 종합 증거 생성:
 * - 교육 이수율, 위험성 평가 현황, 작업중지권 이력, 체크리스트 수행률을 자동 집계
 * - 컴플라이언스 점수(0~100) 및 등급을 산출
 * - AI 기반 개선 권고사항을 자동 생성
 * - 생성된 리포트는 DB에 영구 보존 (삭제 불가)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ComplianceReportService {

    private final ComplianceReportRepository complianceReportRepository;
    private final EducationCompletionRepository educationCompletionRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final CountermeasureRepository countermeasureRepository;
    private final WorkStopReportRepository workStopReportRepository;
    private final ChecklistRepository checklistRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 월간 컴플라이언스 리포트 생성 (또는 기존 리포트 반환)
     */
    @Transactional
    public ComplianceReportResponse generateMonthlyReport(int year, int month, User requestedBy) {
        log.info("[컴플라이언스] 리포트 생성 요청: {}-{}, by={}", year, month, requestedBy.getName());

        // 기존 리포트가 있으면 재생성하지 않고 반환
        Optional<ComplianceReport> existing = complianceReportRepository
                .findByReportYearAndReportMonth(year, month);
        if (existing.isPresent()) {
            log.info("[컴플라이언스] 기존 리포트 반환: id={}", existing.get().getId());
            return ComplianceReportResponse.from(existing.get());
        }

        // 기간 범위 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atTime(LocalTime.MAX);

        // 전체 사용자 수 (교육 이수율 계산용)
        long totalUsers = userRepository.count();

        // ─── 1. 교육 이수 현황 집계 ──────────────────────────────────
        long eduCompleted = educationCompletionRepository.countByCompletedAtBetween(startDt, endDt);
        long eduPassed = educationCompletionRepository.countByCompletedAtBetweenAndQuizPassed(startDt, endDt, true);
        long eduDistinctUsers = educationCompletionRepository.countDistinctPassedUsersByPeriod(startDt, endDt);
        int eduRequired = (int) (totalUsers > 0 ? totalUsers : 20); // 전체 인원 기준
        int eduRate = eduRequired > 0 ? (int) Math.min((eduDistinctUsers * 100) / eduRequired, 100) : 0;

        // ─── 2. 위험성 평가 현황 집계 ────────────────────────────────
        long riskConducted = riskAssessmentRepository.countByCreatedAtBetween(startDt, endDt);
        long riskFindings = riskConducted; // 평가 1건 = 발견사항 1건
        long riskResolved = countermeasureRepository.countCompletedByPeriod(startDt, endDt);
        int riskResolvedRate = riskFindings > 0 ? (int) ((riskResolved * 100) / riskFindings) : 0;

        // ─── 3. 작업중지권 행사 현황 집계 ─────────────────────────────
        long workStopCount = workStopReportRepository.countByCreatedAtBetween(startDt, endDt);

        // ─── 4. 체크리스트 수행 현황 집계 ─────────────────────────────
        long clSubmitted = checklistRepository.countByCreatedAtBetween(startDt, endDt);
        int workDays = Math.min(endDate.getDayOfMonth(), 22); // 근무일 기준
        int clRequired = workDays;
        int clRate = clRequired > 0 ? (int) Math.min((clSubmitted * 100) / clRequired, 100) : 0;

        // ─── 5. 사고/아차사고 통계 (위험 신고 기반) ───────────────────
        // 위험성 평가 중 HIGH/CRITICAL을 사고 건수로 산출
        long highRiskCount = riskAssessmentRepository.countByCreatedAtBetweenAndRiskLevel(startDt, endDt, RiskLevel.HIGH);
        long criticalRiskCount = riskAssessmentRepository.countByCreatedAtBetweenAndRiskLevel(startDt, endDt, RiskLevel.CRITICAL);
        int incidentTotal = (int) (highRiskCount + criticalRiskCount);

        Map<String, Integer> incidentByType = new LinkedHashMap<>();
        workStopReportRepository.countByHazardTypeAndPeriod(startDt, endDt)
                .forEach(row -> incidentByType.put(row[0].toString(), ((Long) row[1]).intValue()));

        Map<String, Integer> incidentBySeverity = new LinkedHashMap<>();
        incidentBySeverity.put("high", (int) highRiskCount);
        incidentBySeverity.put("critical", (int) criticalRiskCount);

        // ─── 6. 컴플라이언스 점수 산출 ───────────────────────────────
        int complianceScore = calculateComplianceScore(
                eduRate, riskResolvedRate, clRate, incidentTotal, (int) workStopCount);

        // ─── 7. AI 개선 권고사항 생성 ────────────────────────────────
        List<String> recommendations = generateRecommendations(
                eduRate, riskResolvedRate, (int) riskConducted, clRate, incidentTotal, (int) workStopCount);

        // ─── 8. DB 저장 ──────────────────────────────────────────────
        ComplianceReport report = ComplianceReport.builder()
                .reportYear(year)
                .reportMonth(month)
                .educationRequired(eduRequired)
                .educationCompleted((int) eduCompleted)
                .educationRate(eduRate)
                .cumulativeLegalHours(0.0) // Phase 2: 누적 법정시간 집계
                .riskAssessmentConducted((int) riskConducted)
                .riskFindings((int) riskFindings)
                .riskResolved((int) riskResolved)
                .riskResolvedRate(riskResolvedRate)
                .workStopExercised((int) workStopCount)
                .checklistRequired(clRequired)
                .checklistSubmitted((int) clSubmitted)
                .checklistRate(clRate)
                .incidentTotal(incidentTotal)
                .incidentByType(toJson(incidentByType))
                .incidentBySeverity(toJson(incidentBySeverity))
                .complianceScore(complianceScore)
                .recommendations(toJson(recommendations))
                .generatedBy(requestedBy)
                .build();

        complianceReportRepository.save(report);

        log.info("[컴플라이언스] 리포트 생성 완료: id={}, score={}, grade={}",
                report.getId(), complianceScore,
                complianceScore >= 90 ? "A" : complianceScore >= 80 ? "B" : complianceScore >= 70 ? "C" : "D");

        return ComplianceReportResponse.from(report);
    }

    /**
     * 특정 연월 리포트 조회
     */
    public ComplianceReportResponse getReport(int year, int month) {
        ComplianceReport report = complianceReportRepository
                .findByReportYearAndReportMonth(year, month)
                .orElseThrow(() -> new CustomException(ErrorCode.COMPLIANCE_REPORT_NOT_FOUND));
        return ComplianceReportResponse.from(report);
    }

    /**
     * 전체 리포트 목록 조회 (최신순)
     */
    public List<ComplianceReportResponse> getAllReports() {
        return complianceReportRepository.findAllByOrderByReportYearDescReportMonthDesc()
                .stream()
                .map(ComplianceReportResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 연도 리포트 목록
     */
    public List<ComplianceReportResponse> getReportsByYear(int year) {
        return complianceReportRepository.findByReportYearOrderByReportMonthDesc(year)
                .stream()
                .map(ComplianceReportResponse::from)
                .collect(Collectors.toList());
    }

    // ===== Private =====

    /**
     * 컴플라이언스 종합 점수 산출 (0~100)
     * 가중치: 교육 30% | 체크리스트 25% | 위험성평가 20% | 사고 15% | 작업중지 10%
     */
    private int calculateComplianceScore(int eduRate, int riskResolvedRate, int clRate,
                                          int incidentTotal, int workStopCount) {
        double eduScore = Math.min(eduRate, 100);
        double riskScore = Math.min(riskResolvedRate, 100);
        double clScore = Math.min(clRate, 100);
        double incidentPenalty = Math.min(incidentTotal * 10, 100);
        double incidentScore = Math.max(100 - incidentPenalty, 0);
        // 작업중지권을 적절히 행사하면 안전 문화가 작동하고 있다고 판단
        double workStopScore = workStopCount > 0 ? 80 : 60;

        double total = (eduScore * 0.30)
                + (clScore * 0.25)
                + (riskScore * 0.20)
                + (incidentScore * 0.15)
                + (workStopScore * 0.10);

        return (int) Math.round(Math.max(0, Math.min(100, total)));
    }

    /**
     * AI 개선 권고사항 자동 생성
     */
    private List<String> generateRecommendations(int eduRate, int riskResolvedRate,
                                                   int riskConducted, int clRate,
                                                   int incidentTotal, int workStopCount) {
        List<String> rec = new ArrayList<>();

        if (eduRate < 80) {
            rec.add("안전 교육 이수율이 목표(80%) 미만입니다. 미이수자에 대한 추가 교육 일정을 수립하세요.");
        }
        if (eduRate < 50) {
            rec.add("교육 이수율이 심각하게 낮습니다. 경영책임자 보고 및 긴급 교육 계획 수립이 필요합니다.");
        }
        if (riskResolvedRate < 70) {
            rec.add("위험성 평가 발견사항의 해결율이 낮습니다. 미해결 항목에 대한 시정 조치 기한을 설정하세요.");
        }
        if (riskConducted < 5) {
            rec.add("위험성 평가 실시 횟수가 부족합니다. 법정 기준에 맞춰 정기적인 평가를 실시하세요.");
        }
        if (clRate < 80) {
            rec.add("안전 점검 체크리스트 수행률이 부족합니다. 일일 점검 의무화 및 모니터링을 강화하세요.");
        }
        if (incidentTotal > 5) {
            rec.add("월간 사고/아차사고 건수가 많습니다. 반복 발생 유형에 대한 집중 개선 활동을 진행하세요.");
        }
        if (workStopCount > 0) {
            rec.add("작업중지권 행사 이력이 있습니다. 근본 원인 분석을 통해 유사 위험의 사전 예방 체계를 구축하세요.");
        }
        if (rec.isEmpty()) {
            rec.add("전반적인 안전 관리 수준이 양호합니다. 지속적인 개선 활동을 유지하세요.");
        }
        rec.add("중대재해처벌법 제4조에 따른 안전보건관리체계 구축 의무를 지속적으로 점검하세요.");

        return rec;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("[컴플라이언스] JSON 변환 실패", e);
            return "[]";
        }
    }
}
