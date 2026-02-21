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

    private final HazardReportRepository hazardReportRepository;
    private final HazardReportPhotoRepository hazardReportPhotoRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final StorageService storageService;
    private final ActivityLogService activityLogService;
    private final CycleRewardService cycleRewardService;
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

        report = hazardReportRepository.save(report);

        savePhotoMetadata(report, HazardPhotoStage.HAZARD, photo, storedPath, 0);

        CycleRewardService.RewardGrant tier1Reward = null;

        try {
            GeminiAnalysisResult aiResult = geminiService.analyzeImage(
                    photo.getBytes(),
                    photo.getContentType());

            report.applyAiAnalysis(
                    aiResult.getRiskLevel(),
                    aiResult.getRiskFactor(),
                    toJson(aiResult.getRemediationSteps()),
                    aiResult.getReferenceCode(),
                    aiResult.getUsageMetadata() != null ? aiResult.getUsageMetadata().getTotalTokenCount() : null,
                    LocalDateTime.now());

            tier1Reward = cycleRewardService.awardTier1(report);
        } catch (IOException e) {
            log.error("Hazard Cycle AI 분석 이미지 읽기 실패: reportId={}", report.getId(), e);
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED, "이미지 분석 처리에 실패했습니다.");
        }

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
            aiAnalysis = HazardCycleResponse.AiAnalysisDetail.builder()
                    .riskLevel(report.getAiRiskLevel())
                    .riskFactor(report.getAiRiskFactor())
                    .remediationSteps(parseSteps(report.getAiRemediationSteps()))
                    .referenceCode(report.getAiReferenceCode())
                    .analyzedAt(report.getAiAnalyzedAt())
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
