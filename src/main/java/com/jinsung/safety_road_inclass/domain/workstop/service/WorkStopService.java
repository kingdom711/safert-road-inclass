package com.jinsung.safety_road_inclass.domain.workstop.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.workstop.dto.*;
import com.jinsung.safety_road_inclass.domain.workstop.entity.*;
import com.jinsung.safety_road_inclass.domain.workstop.repository.RetaliationReportRepository;
import com.jinsung.safety_road_inclass.domain.workstop.repository.WorkStopProtectionRepository;
import com.jinsung.safety_road_inclass.domain.workstop.repository.WorkStopReportRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WorkStopService - 작업중지권 비즈니스 로직
 *
 * 중대재해처벌법 증거 보전 핵심:
 * - 모든 신고·상태변경은 DB에 불변 기록
 * - 타임스탬프는 서버 시각 기준 (클라이언트 조작 방지)
 * - 보호 기간 자동 생성 (30일)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkStopService {

    private final WorkStopReportRepository reportRepository;
    private final WorkStopProtectionRepository protectionRepository;
    private final RetaliationReportRepository retaliationRepository;

    /**
     * 작업중지 신고 등록
     */
    @Transactional
    public WorkStopReportResponse createReport(WorkStopReportRequest request, User reporter) {
        log.info("[작업중지] 신고 등록: userId={}, hazardType={}, zone={}",
                reporter.getId(), request.getHazardType(), request.getZone());

        WorkStopReport report = WorkStopReport.builder()
                .reporter(reporter)
                .hazardType(request.getHazardType())
                .description(request.getDescription())
                .zone(request.getZone())
                .isAnonymous(request.getIsAnonymous())
                .photoUrl(request.getPhotoUrl())
                .build();

        // 초기 상태 이력 기록
        report.addInitialHistory();

        // 보호 기간 자동 생성 (30일)
        WorkStopProtection protection = WorkStopProtection.builder()
                .worker(reporter)
                .workStopReport(report)
                .build();

        reportRepository.save(report);
        protectionRepository.save(protection);

        log.info("[작업중지] 신고 완료: reportId={}, protectionEndDate={}",
                report.getId(), protection.getEndDate());

        return WorkStopReportResponse.from(report);
    }

    /**
     * 신고 목록 조회 (상태 필터 가능)
     */
    public List<WorkStopReportResponse> getReports(ReportStatus status) {
        List<WorkStopReport> reports;
        if (status != null) {
            reports = reportRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            reports = reportRepository.findAllByOrderByCreatedAtDesc();
        }
        return reports.stream()
                .map(WorkStopReportResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 신고 상세 조회
     */
    public WorkStopReportResponse getReportById(Long reportId) {
        WorkStopReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_STOP_NOT_FOUND));
        return WorkStopReportResponse.from(report);
    }

    /**
     * 신고 상태 업데이트 (이력 자동 기록)
     */
    @Transactional
    public WorkStopReportResponse updateStatus(Long reportId, WorkStopStatusUpdateRequest request, User updater) {
        WorkStopReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_STOP_NOT_FOUND));

        log.info("[작업중지] 상태 변경: reportId={}, {} → {}, updater={}",
                reportId, report.getStatus(), request.getStatus(), updater.getName());

        ReportStatus newStatus = request.getStatus();

        if (newStatus == ReportStatus.RESOLVED) {
            report.resolve(updater, request.getNote(), request.getActionPlan(), request.getActionMethod());
        } else if (newStatus == ReportStatus.RESUMED) {
            report.resume(updater, request.getResumeVerification());
        } else {
            report.changeStatus(newStatus, updater, request.getNote());
        }

        return WorkStopReportResponse.from(report);
    }

    /**
     * 통계 조회
     */
    public WorkStopStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        long totalReports = reportRepository.count();
        long recentReports = reportRepository.countByCreatedAtBetween(thirtyDaysAgo, now);
        List<ReportStatus> resolvedStatuses = Arrays.asList(ReportStatus.RESOLVED, ReportStatus.RESUMED);
        long resolvedCount = reportRepository.countByCreatedAtBetweenAndStatusIn(thirtyDaysAgo, now, resolvedStatuses);
        int resolutionRate = recentReports > 0 ? (int) ((resolvedCount * 100) / recentReports) : 100;

        // 평균 대응 시간 계산
        List<WorkStopReport> recentList = reportRepository.findByPeriod(thirtyDaysAgo, now);
        long avgResponseMinutes = calculateAvgResponseMinutes(recentList);

        // 위험 유형별 통계
        Map<String, Long> byHazardType = new LinkedHashMap<>();
        reportRepository.countByHazardTypeAndPeriod(thirtyDaysAgo, now)
                .forEach(row -> byHazardType.put(((HazardType) row[0]).getLabel(), (Long) row[1]));

        long retaliationCount = retaliationRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long activeProtections = protectionRepository.countActiveProtections(now);

        return WorkStopStatsResponse.builder()
                .totalReports(totalReports)
                .recentReports(recentReports)
                .resolvedCount(resolvedCount)
                .resolutionRate(resolutionRate)
                .avgResponseMinutes(avgResponseMinutes)
                .retaliationCount(retaliationCount)
                .activeProtections(activeProtections)
                .byHazardType(byHazardType)
                .build();
    }

    /**
     * 보호 기간 조회
     */
    public List<WorkStopProtectionResponse> getProtections(User worker) {
        List<WorkStopProtection> protections;
        if (worker != null) {
            protections = protectionRepository.findActiveProtections(worker, LocalDateTime.now());
        } else {
            protections = protectionRepository.findAll();
        }
        return protections.stream()
                .map(WorkStopProtectionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 보복 신고 등록
     */
    @Transactional
    public void createRetaliationReport(RetaliationReportRequest request, User worker) {
        WorkStopProtection protection = protectionRepository.findById(request.getProtectionId())
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_STOP_PROTECTION_NOT_FOUND));

        log.warn("[작업중지] 보복 신고 접수: workerId={}, protectionId={}, type={}",
                worker.getId(), request.getProtectionId(), request.getRetaliationType());

        RetaliationReport report = RetaliationReport.builder()
                .worker(worker)
                .protection(protection)
                .retaliationType(request.getRetaliationType())
                .description(request.getDescription())
                .evidenceUrl(request.getEvidenceUrl())
                .build();

        retaliationRepository.save(report);
    }

    /**
     * 기간별 작업중지 이력 조회 (컴플라이언스 리포트용)
     */
    public List<WorkStopReportResponse> getReportsByPeriod(LocalDateTime start, LocalDateTime end) {
        return reportRepository.findByPeriod(start, end).stream()
                .map(WorkStopReportResponse::from)
                .collect(Collectors.toList());
    }

    // === Private ===

    private long calculateAvgResponseMinutes(List<WorkStopReport> reports) {
        List<Long> responseTimes = reports.stream()
                .filter(r -> r.getStatusHistories().size() >= 2)
                .map(r -> {
                    LocalDateTime reported = r.getStatusHistories().get(0).getChangedAt();
                    LocalDateTime firstResponse = r.getStatusHistories().get(1).getChangedAt();
                    return Duration.between(reported, firstResponse).toMinutes();
                })
                .collect(Collectors.toList());

        if (responseTimes.isEmpty()) return 0;
        return responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();
    }
}
