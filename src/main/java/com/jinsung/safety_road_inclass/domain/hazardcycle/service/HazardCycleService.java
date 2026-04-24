package com.jinsung.safety_road_inclass.domain.hazardcycle.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinsung.safety_road_inclass.domain.activity.entity.ActivityType;
import com.jinsung.safety_road_inclass.domain.activity.service.ActivityLogService;
import com.jinsung.safety_road_inclass.domain.ai.dto.gemini.GeminiAnalysisResult;
import com.jinsung.safety_road_inclass.domain.ai.service.GeminiService;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.dto.*;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.*;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportAckRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportPhotoRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import com.jinsung.safety_road_inclass.global.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * HazardCycleService - 위험 발견 > AI 분석 > 조치 완료 사이클 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HazardCycleService {

    private static final int DAILY_CYCLE_LIMIT = 10;
    private static final int CACHE_TTL_DAYS = 7;

    private final HazardReportRepository hazardReportRepository;
    private final HazardReportPhotoRepository hazardReportPhotoRepository;
    private final HazardReportAckRepository hazardReportAckRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final StorageService storageService;
    private final ActivityLogService activityLogService;
    private final CycleRewardService cycleRewardService;
    private final RiskAssessmentPdfService riskAssessmentPdfService;
    private final HazardLogExcelService hazardLogExcelService;
    private final ObjectMapper objectMapper;

    /**
     * Step 1+2: 위험 사진 업로드 + AI 분석 + 1단계 보상
     */
    @Transactional
    public HazardCycleResponse createHazardCycle(
            Long reporterId,
            MultipartFile photo,
            HazardCycleCreateRequest request) {

        User reporter = getReporter(reporterId);
        HazardCycleCreateRequest safeRequest = request != null ? request : new HazardCycleCreateRequest();

        return createHazardCycleInternal(
                reporter,
                photo,
                safeRequest,
                LocalDateTime.now(),
                false
        );
    }

    /**
     * Step 3: 조치 완료 사진 업로드 + 2단계 보상
     */
    @Transactional
    public HazardCycleResponse completeCycle(
            Long reporterId,
            Long cycleId,
            MultipartFile completionPhoto,
            CompletionUploadRequest request) {

        if (completionPhoto == null || completionPhoto.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "조치 완료 사진은 필수입니다.");
        }

        HazardReport report = hazardReportRepository.findById(cycleId)
                .orElseThrow(() -> new CustomException(ErrorCode.HAZARD_CYCLE_NOT_FOUND));

        validateOwnership(reporterId, report);

        if (report.getStatus() == CycleStatus.ACTION_COMPLETED) {
            throw new CustomException(ErrorCode.HAZARD_CYCLE_ALREADY_COMPLETED);
        }

        String completionPath = storageService.store(completionPhoto);

        report.completeAction(
                completionPath,
                request != null ? request.getNote() : null,
                LocalDateTime.now());

        savePhotoMetadata(report, HazardPhotoStage.COMPLETION, completionPhoto, completionPath, 0);

        CycleRewardService.RewardGrant tier2 = cycleRewardService.awardTier2(report);

        hazardReportRepository.save(report);

        activityLogService.log(
                reporterId,
                ActivityType.HAZARD_CYCLE_COMPLETED,
                "Hazard Cycle 조치 완료",
                "조치 완료 보상이 지급되었습니다.",
                "{\"hazardCycleId\":" + report.getId() + "}");

        log.info("Hazard Cycle 완료: cycleId={}, reporterId={}", cycleId, reporterId);

        return toResponse(report, null, tier2);
    }

    public Page<HazardCycleResponse> getMyCycles(Long reporterId, String status, Pageable pageable) {
        Page<HazardReport> page;

        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            page = hazardReportRepository.findByReporterIdOrderByReportedAtDesc(reporterId, pageable);
        } else {
            CycleStatus cycleStatus;
            try {
                cycleStatus = CycleStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "지원하지 않는 status입니다: " + status);
            }

            page = hazardReportRepository.findByReporterIdAndStatusOrderByReportedAtDesc(reporterId, cycleStatus, pageable);
        }

        return page.map(report -> toResponse(report, null, null));
    }

    public HazardCycleResponse getCycleDetail(Long reporterId, Long cycleId) {
        HazardReport report = hazardReportRepository.findById(cycleId)
                .orElseThrow(() -> new CustomException(ErrorCode.HAZARD_CYCLE_NOT_FOUND));

        validateOwnership(reporterId, report);

        return toResponse(report, null, null);
    }

    /**
     * 동료 확인(peer ack)용 사이클 요약 조회 — 소유권 검증을 건너뛴다.
     * 인증된 사용자라면 누구나 조회 가능 (AI 분석 결과 공유 목적).
     */
    public HazardCycleResponse getCycleSummary(Long cycleId) {
        HazardReport report = hazardReportRepository.findById(cycleId)
                .orElseThrow(() -> new CustomException(ErrorCode.HAZARD_CYCLE_NOT_FOUND));
        return toResponse(report, null, null);
    }

    /**
     * 동료 근로자 위험요인 확인 (산안법 제36조 2항 근로자 참여 증빙).
     * 본인이 신고한 사이클은 확인 불가, 중복 확인 방지.
     */
    @Transactional
    public HazardAckResponse ackHazardCycle(Long ackerId, Long cycleId, HazardAckRequest request) {
        HazardReport report = hazardReportRepository.findById(cycleId)
                .orElseThrow(() -> new CustomException(ErrorCode.HAZARD_CYCLE_NOT_FOUND));

        if (Objects.equals(report.getReporter().getId(), ackerId)) {
            throw new CustomException(ErrorCode.HAZARD_ACK_SELF_FORBIDDEN);
        }

        if (hazardReportAckRepository.existsByHazardReportIdAndAckerId(cycleId, ackerId)) {
            throw new CustomException(ErrorCode.HAZARD_ACK_DUPLICATE);
        }

        User acker = userRepository.findById(ackerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        HazardReportAck ack = HazardReportAck.builder()
                .hazardReport(report)
                .acker(acker)
                .comment(request != null ? request.getComment() : null)
                .ackedAt(LocalDateTime.now())
                .build();

        ack = hazardReportAckRepository.save(ack);

        activityLogService.log(
                ackerId,
                ActivityType.HAZARD_CYCLE_ACKED,
                "Hazard Cycle 동료 확인",
                "동료 근로자가 위험요인을 확인했습니다.",
                "{\"hazardCycleId\":" + cycleId + "}");

        log.info("Hazard Cycle 근로자 참여 확인: cycleId={}, ackerId={}", cycleId, ackerId);

        return HazardAckResponse.builder()
                .id(ack.getId())
                .hazardReportId(cycleId)
                .ackerId(ackerId)
                .ackerName(acker.getName())
                .comment(ack.getComment())
                .ackedAt(ack.getAckedAt())
                .build();
    }

    public List<HazardAckResponse> listAcks(Long cycleId) {
        if (!hazardReportRepository.existsById(cycleId)) {
            throw new CustomException(ErrorCode.HAZARD_CYCLE_NOT_FOUND);
        }
        return hazardReportAckRepository.findByHazardReportIdOrderByAckedAtAsc(cycleId).stream()
                .map(a -> HazardAckResponse.builder()
                        .id(a.getId())
                        .hazardReportId(cycleId)
                        .ackerId(a.getAcker().getId())
                        .ackerName(a.getAcker().getName())
                        .comment(a.getComment())
                        .ackedAt(a.getAckedAt())
                        .build())
                .toList();
    }

    /**
     * QR 검증 — 공개 엔드포인트에서 호출. certNumber 기반으로 원본 사이클의 해시/상태를 반환.
     */
    public HazardVerifyResponse verifyByCertNumber(String certNumber) {
        return hazardReportRepository.findByCertNumber(certNumber)
                .map(r -> HazardVerifyResponse.builder()
                        .valid(r.getIntegrityHash() != null)
                        .cycleId(r.getId())
                        .certNumber(r.getCertNumber())
                        .integrityHash(r.getIntegrityHash())
                        .prevHash(r.getPrevHash())
                        .reporterName(r.getReporter() != null ? r.getReporter().getName() : null)
                        .reportedAt(r.getReportedAt())
                        .riskLevel(r.getAiRiskLevel())
                        .riskScore(r.getAiRiskScore())
                        .status(r.getStatus() != null ? r.getStatus().name() : null)
                        .build())
                .orElse(HazardVerifyResponse.builder().valid(false).build());
    }

    /**
     * 월간 위험요인 개선대장 Excel 생성 (ADMIN 전용).
     * 고용노동부 점검관 제출용 사업장 전체 대장.
     */
    public byte[] renderMonthlyLogExcel(int year, int month) {
        return hazardLogExcelService.generateMonthly(year, month);
    }

    /**
     * 위험성평가표 PDF 생성 (고용노동부 제출용).
     * 본인 또는 ADMIN/INSPECTOR 권한 필요.
     */
    public byte[] renderRiskAssessmentPdf(Long reporterId, Long cycleId) {
        HazardReport report = hazardReportRepository.findById(cycleId)
                .orElseThrow(() -> new CustomException(ErrorCode.HAZARD_CYCLE_NOT_FOUND));

        validateOwnership(reporterId, report);

        if (report.getAiAnalyzedAt() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "AI 분석이 완료된 사이클만 위험성평가표를 발급할 수 있습니다.");
        }

        return riskAssessmentPdfService.generate(report);
    }

    /**
     * 오프라인 배치 동기화
     */
    @Transactional
    public CycleSyncResponse syncOfflineCycles(
            Long reporterId,
            List<CycleSyncRequest> cycles,
            List<MultipartFile> photos) {

        User reporter = getReporter(reporterId);

        Map<String, MultipartFile> photoByName = new HashMap<>();
        if (photos != null) {
            for (MultipartFile photo : photos) {
                if (photo != null && photo.getOriginalFilename() != null && !photoByName.containsKey(photo.getOriginalFilename())) {
                    photoByName.put(photo.getOriginalFilename(), photo);
                }
            }
        }

        List<CycleSyncResponse.SyncResult> results = new ArrayList<>();
        int success = 0;
        int failed = 0;

        for (CycleSyncRequest cycle : cycles) {
            try {
                MultipartFile photo = photoByName.get(cycle.getPhotoFileName());
                if (photo == null) {
                    throw new CustomException(ErrorCode.INVALID_INPUT,
                            "매칭되는 사진이 없습니다: " + cycle.getPhotoFileName());
                }

                HazardCycleCreateRequest createRequest = new HazardCycleCreateRequest();
                createRequest.setDescription(cycle.getDescription());
                createRequest.setLocation(cycle.getLocation());
                createRequest.setClientTempId(cycle.getClientTempId());

                LocalDateTime reportedAt = cycle.getReportedAt() != null ? cycle.getReportedAt() : LocalDateTime.now();

                HazardCycleResponse created = createHazardCycleInternal(
                        reporter,
                        photo,
                        createRequest,
                        reportedAt,
                        true);

                results.add(CycleSyncResponse.SyncResult.builder()
                        .clientTempId(cycle.getClientTempId())
                        .serverId(created.getId())
                        .status(created.getStatus())
                        .aiAnalysis(created.getAiAnalysis())
                        .tier1Reward(created.getTier1Reward())
                        .error(null)
                        .build());
                success++;
            } catch (Exception e) {
                String errorMessage = e instanceof CustomException ce
                        ? ce.getResponseMessage()
                        : "동기화 처리 중 오류가 발생했습니다.";

                log.warn("오프라인 동기화 실패: clientTempId={}, error={}", cycle.getClientTempId(), errorMessage);

                results.add(CycleSyncResponse.SyncResult.builder()
                        .clientTempId(cycle.getClientTempId())
                        .status("FAILED")
                        .error(errorMessage)
                        .build());
                failed++;
            }
        }

        return CycleSyncResponse.builder()
                .syncedCount(success)
                .failedCount(failed)
                .results(results)
                .build();
    }

    public CycleStatsResponse getStats(Long reporterId) {
        long totalCycles = hazardReportRepository.countByReporterId(reporterId);
        long completedCycles = hazardReportRepository.countByReporterIdAndStatus(reporterId, CycleStatus.ACTION_COMPLETED);
        long pendingCycles = totalCycles - completedCycles;

        double completionRate = totalCycles == 0
                ? 0.0
                : Math.round((completedCycles * 10000.0 / totalCycles)) / 100.0;

        Integer totalPoints = hazardReportRepository.sumPointsByReporterId(reporterId);

        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusDays(6).atTime(LocalTime.MAX);

        long thisWeekCycles = hazardReportRepository.countByReporterIdAndReportedAtBetween(
                reporterId,
                weekStart,
                weekEnd);

        Map<String, Long> riskLevelDistribution = new LinkedHashMap<>();
        for (Object[] row : hazardReportRepository.countByRiskLevel(reporterId)) {
            String riskLevel = (String) row[0];
            Long count = (Long) row[1];
            riskLevelDistribution.put(riskLevel, count);
        }

        return CycleStatsResponse.builder()
                .totalCycles(totalCycles)
                .completedCycles(completedCycles)
                .pendingCycles(Math.max(pendingCycles, 0))
                .completionRate(completionRate)
                .totalPointsEarned(totalPoints != null ? totalPoints : 0)
                .thisWeekCycles(thisWeekCycles)
                .riskLevelDistribution(riskLevelDistribution)
                .build();
    }

    private HazardCycleResponse createHazardCycleInternal(
            User reporter,
            MultipartFile photo,
            HazardCycleCreateRequest request,
            LocalDateTime reportedAt,
            boolean syncedFromOffline) {

        if (photo == null || photo.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "위험 사진은 필수입니다.");
        }

        if (request.getClientTempId() != null && !request.getClientTempId().isBlank()) {
            Optional<HazardReport> existing = hazardReportRepository.findByClientTempId(request.getClientTempId());
            if (existing.isPresent()) {
                HazardReport existingReport = existing.get();
                validateOwnership(reporter.getId(), existingReport);
                return toResponse(existingReport, null, null);
            }
        }

        validateDailyLimit(reporter.getId(), reportedAt);

        byte[] photoBytes;
        try {
            photoBytes = photo.getBytes();
        } catch (IOException e) {
            log.error("Hazard Cycle 사진 바이트 읽기 실패", e);
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "사진 읽기 처리에 실패했습니다.");
        }

        String photoHash = sha256Hex(photoBytes);

        String storedPath = storageService.store(photo);

        HazardReport report = HazardReport.builder()
                .reporter(reporter)
                .hazardPhotoPath(storedPath)
                .hazardDescription(request.getDescription())
                .locationDescription(request.getLocation())
                .reportedAt(reportedAt)
                .status(CycleStatus.HAZARD_REPORTED)
                .clientTempId(request.getClientTempId())
                .syncedFromOffline(syncedFromOffline)
                .build();

        report.setPhotoSha256(photoHash);
        report = hazardReportRepository.save(report);

        savePhotoMetadata(report, HazardPhotoStage.HAZARD, photo, storedPath, 0);

        CycleRewardService.RewardGrant tier1Reward = null;

        // 캐시 히트 확인: 동일 사진 해시의 최근 분석 결과 재사용
        Optional<HazardReport> cacheHit = findCacheHit(photoHash, report.getId());
        if (cacheHit.isPresent()) {
            HazardReport source = cacheHit.get();
            report.copyAiAnalysisFrom(source, LocalDateTime.now());
            tier1Reward = cycleRewardService.awardTier1(report);

            sealIntegrity(report);
            hazardReportRepository.save(report);

            activityLogService.log(
                    reporter.getId(),
                    ActivityType.HAZARD_CYCLE_REPORTED,
                    "Hazard Cycle 등록(캐시)",
                    "동일 사진 해시 재분석 — 캐시 재사용",
                    "{\"hazardCycleId\":" + report.getId() + ",\"cacheSourceId\":" + source.getId() + "}");

            log.info("Hazard Cycle 캐시 히트: cycleId={}, sourceId={}, sha256={}",
                    report.getId(), source.getId(), photoHash.substring(0, 12));

            return toResponse(report, tier1Reward, null);
        }

        try {
            GeminiAnalysisResult aiResult = geminiService.analyzeImage(
                    photoBytes,
                    photo.getContentType());

            // 서버측 일관성 보정: severity × likelihood = riskScore
            Integer computedRiskScore = null;
            if (aiResult.getSeverity() != null && aiResult.getSeverity().getScore() != null
                    && aiResult.getLikelihood() != null && aiResult.getLikelihood().getScore() != null) {
                computedRiskScore = aiResult.getSeverity().getScore() * aiResult.getLikelihood().getScore();
            } else if (aiResult.getRiskScore() != null) {
                computedRiskScore = aiResult.getRiskScore();
            }

            report.applyAiAnalysis(
                    aiResult.getRiskLevel(),
                    aiResult.getRiskFactor(),
                    toJson(aiResult.getRemediationSteps()),
                    aiResult.getReferenceCode() != null ? aiResult.getReferenceCode() : aiResult.getKoshaGuide(),
                    aiResult.getUsageMetadata() != null ? aiResult.getUsageMetadata().getTotalTokenCount() : null,
                    LocalDateTime.now());

            report.applyExtendedAnalysis(
                    aiResult.getHazardClassification(),
                    aiResult.getUnsafeCondition(),
                    aiResult.getUnsafeAct(),
                    aiResult.getPossibleAccident(),
                    aiResult.getSeverity() != null ? aiResult.getSeverity().getScore() : null,
                    aiResult.getSeverity() != null ? aiResult.getSeverity().getRationale() : null,
                    aiResult.getLikelihood() != null ? aiResult.getLikelihood().getScore() : null,
                    aiResult.getLikelihood() != null ? aiResult.getLikelihood().getRationale() : null,
                    computedRiskScore,
                    serializeJson(aiResult.getLegalBasis()),
                    aiResult.getKoshaGuide(),
                    serializeJson(aiResult.getControlMeasures()),
                    aiResult.getResponsibleRole(),
                    aiResult.getDueDays(),
                    aiResult.getConfidence());

            tier1Reward = cycleRewardService.awardTier1(report);
        } catch (RuntimeException e) {
            log.error("Hazard Cycle AI 분석 실패: reportId={}", report.getId(), e);
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "이미지 분석 처리에 실패했습니다.");
        }

        sealIntegrity(report);
        hazardReportRepository.save(report);

        activityLogService.log(
                reporter.getId(),
                ActivityType.HAZARD_CYCLE_REPORTED,
                "Hazard Cycle 등록",
                "위험 발견 등록 및 AI 분석 완료",
                "{\"hazardCycleId\":" + report.getId() + "}");

        log.info("Hazard Cycle 생성 완료: cycleId={}, reporterId={}, offline={}",
                report.getId(), reporter.getId(), syncedFromOffline);

        return toResponse(report, tier1Reward, null);
    }

    private void savePhotoMetadata(HazardReport report,
                                   HazardPhotoStage stage,
                                   MultipartFile file,
                                   String storedPath,
                                   int displayOrder) {
        HazardReportPhoto photo = HazardReportPhoto.builder()
                .photoStage(stage)
                .storedPath(storedPath)
                .originalName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .displayOrder(displayOrder)
                .build();

        report.addPhoto(photo);
        hazardReportPhotoRepository.save(photo);
    }

    private HazardCycleResponse toResponse(HazardReport report,
                                           CycleRewardService.RewardGrant tier1Override,
                                           CycleRewardService.RewardGrant tier2Override) {

        HazardCycleResponse.AiAnalysisDetail aiAnalysis = null;
        if (report.getAiAnalyzedAt() != null) {
            HazardCycleResponse.ScoreDetail severity = null;
            if (report.getAiSeverityScore() != null) {
                severity = HazardCycleResponse.ScoreDetail.builder()
                        .score(report.getAiSeverityScore())
                        .rationale(report.getAiSeverityRationale())
                        .build();
            }

            HazardCycleResponse.ScoreDetail likelihood = null;
            if (report.getAiLikelihoodScore() != null) {
                likelihood = HazardCycleResponse.ScoreDetail.builder()
                        .score(report.getAiLikelihoodScore())
                        .rationale(report.getAiLikelihoodRationale())
                        .build();
            }

            List<HazardCycleResponse.LegalReference> legalBasis =
                    deserializeJson(report.getAiLegalBasisJson(),
                            new TypeReference<List<HazardCycleResponse.LegalReference>>() {
                            });

            HazardCycleResponse.ControlMeasures controlMeasures =
                    deserializeJson(report.getAiControlMeasuresJson(),
                            new TypeReference<HazardCycleResponse.ControlMeasures>() {
                            });

            aiAnalysis = HazardCycleResponse.AiAnalysisDetail.builder()
                    .riskLevel(report.getAiRiskLevel())
                    .riskFactor(report.getAiRiskFactor())
                    .remediationSteps(parseSteps(report.getAiRemediationSteps()))
                    .referenceCode(report.getAiReferenceCode())
                    .analyzedAt(report.getAiAnalyzedAt())
                    .hazardClassification(report.getAiHazardClassification())
                    .unsafeCondition(report.getAiUnsafeCondition())
                    .unsafeAct(report.getAiUnsafeAct())
                    .possibleAccident(report.getAiPossibleAccident())
                    .severity(severity)
                    .likelihood(likelihood)
                    .riskScore(report.getAiRiskScore())
                    .legalBasis(legalBasis)
                    .koshaGuide(report.getAiKoshaGuide())
                    .controlMeasures(controlMeasures)
                    .responsibleRole(report.getAiResponsibleRole())
                    .dueDays(report.getAiDueDays())
                    .confidence(report.getAiConfidence())
                    .cacheSourceId(report.getAiCacheSourceId())
                    .build();
        }

        HazardCycleResponse.RewardDetail tier1 = null;
        if (tier1Override != null) {
            tier1 = HazardCycleResponse.RewardDetail.builder()
                    .pointsAwarded(tier1Override.getPointsAwarded())
                    .newBalance(tier1Override.getNewBalance())
                    .message(tier1Override.getMessage())
                    .build();
        } else if (report.getTier1PointsAwarded() > 0) {
            tier1 = HazardCycleResponse.RewardDetail.builder()
                    .pointsAwarded(report.getTier1PointsAwarded())
                    .newBalance(0)
                    .message("1단계 보상 지급됨")
                    .build();
        }

        HazardCycleResponse.RewardDetail tier2 = null;
        if (tier2Override != null) {
            tier2 = HazardCycleResponse.RewardDetail.builder()
                    .pointsAwarded(tier2Override.getPointsAwarded())
                    .newBalance(tier2Override.getNewBalance())
                    .message(tier2Override.getMessage())
                    .build();
        } else if (report.getTier2PointsAwarded() > 0) {
            tier2 = HazardCycleResponse.RewardDetail.builder()
                    .pointsAwarded(report.getTier2PointsAwarded())
                    .newBalance(0)
                    .message("2단계 보상 지급됨")
                    .build();
        }

        return HazardCycleResponse.builder()
                .id(report.getId())
                .status(report.getStatus().name())
                .hazardPhotoUrl(toFileUrl(report.getHazardPhotoPath()))
                .hazardDescription(report.getHazardDescription())
                .locationDescription(report.getLocationDescription())
                .reportedAt(report.getReportedAt())
                .aiAnalysis(aiAnalysis)
                .completionPhotoUrl(toFileUrl(report.getCompletionPhotoPath()))
                .completionNote(report.getCompletionNote())
                .completedAt(report.getCompletedAt())
                .tier1Reward(tier1)
                .tier2Reward(tier2)
                .totalPointsEarned(report.getTotalPointsAwarded())
                .build();
    }

    private User getReporter(Long reporterId) {
        return userRepository.findById(reporterId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateOwnership(Long reporterId, HazardReport report) {
        if (!Objects.equals(report.getReporter().getId(), reporterId)) {
            throw new CustomException(ErrorCode.HAZARD_CYCLE_FORBIDDEN);
        }
    }

    private void validateDailyLimit(Long reporterId, LocalDateTime reportedAt) {
        LocalDate date = reportedAt.toLocalDate();
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        long todayCount = hazardReportRepository.countByReporterIdAndReportedAtBetween(reporterId, dayStart, dayEnd);
        if (todayCount >= DAILY_CYCLE_LIMIT) {
            throw new CustomException(ErrorCode.HAZARD_DAILY_LIMIT_EXCEEDED,
                    "일일 Hazard Cycle 생성 한도(10건)를 초과했습니다.");
        }
    }

    private String toJson(List<String> steps) {
        if (steps == null || steps.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(steps);
        } catch (JsonProcessingException e) {
            log.warn("조치 단계 JSON 직렬화 실패", e);
            return null;
        }
    }

    private String serializeJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List<?> list && list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("위험성평가 필드 JSON 직렬화 실패", e);
            return null;
        }
    }

    /**
     * 무결성 해시체인 봉인.
     * - cert_number: UUID
     * - prev_hash: 직전 봉인된 사이클의 integrity_hash (없으면 "genesis")
     * - integrity_hash: SHA-256(cycleId | reporterId | reportedAt | aiFields | photoSha256 | prevHash)
     *
     * 이미 봉인된 경우 재계산하지 않는다.
     */
    private void sealIntegrity(HazardReport report) {
        if (report.getIntegrityHash() != null) {
            return;
        }

        String prevHash;
        List<HazardReport> recent = hazardReportRepository.findMostRecentSealed(
                org.springframework.data.domain.PageRequest.of(0, 1));
        if (recent.isEmpty() || Objects.equals(recent.get(0).getId(), report.getId())) {
            prevHash = "genesis";
        } else {
            prevHash = recent.get(0).getIntegrityHash();
        }

        String certNumber = java.util.UUID.randomUUID().toString();
        String payload = String.join("|",
                String.valueOf(report.getId()),
                String.valueOf(report.getReporter().getId()),
                String.valueOf(report.getReportedAt()),
                nullToEmpty(report.getAiRiskLevel()),
                nullToEmpty(report.getAiRiskFactor()),
                String.valueOf(report.getAiRiskScore()),
                nullToEmpty(report.getAiKoshaGuide()),
                nullToEmpty(report.getPhotoSha256()),
                prevHash);

        String integrityHash = sha256Hex(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        report.sealIntegrity(certNumber, prevHash, integrityHash);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String sha256Hex(byte[] bytes) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘 미지원", e);
        }
    }

    private Optional<HazardReport> findCacheHit(String photoHash, Long currentReportId) {
        if (photoHash == null) {
            return Optional.empty();
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(CACHE_TTL_DAYS);
        List<HazardReport> hits = hazardReportRepository.findRecentAnalyzedByHash(photoHash, cutoff);
        return hits.stream()
                .filter(r -> !Objects.equals(r.getId(), currentReportId))
                .findFirst();
    }

    private <T> T deserializeJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.warn("위험성평가 필드 JSON 역직렬화 실패: {}", json, e);
            return null;
        }
    }

    private List<String> parseSteps(String stepsJson) {
        if (stepsJson == null || stepsJson.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(stepsJson, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("조치 단계 JSON 역직렬화 실패: {}", stepsJson, e);
            return List.of();
        }
    }

    private String toFileUrl(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        return "/api/v1/files/" + storedPath;
    }
}
