package com.jinsung.safety_road_inclass.domain.hazardcycle.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinsung.safety_road_inclass.domain.activity.service.ActivityLogService;
import com.jinsung.safety_road_inclass.domain.ai.dto.gemini.GeminiAnalysisResult;
import com.jinsung.safety_road_inclass.domain.ai.service.GeminiService;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.dto.*;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.CycleStatus;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReport;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportAckRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportPhotoRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import com.jinsung.safety_road_inclass.global.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HazardCycleService 단위 테스트")
class HazardCycleServiceTest {

    @Mock
    private HazardReportRepository hazardReportRepository;

    @Mock
    private HazardReportPhotoRepository hazardReportPhotoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeminiService geminiService;

    @Mock
    private StorageService storageService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private CycleRewardService cycleRewardService;

    @Mock
    private HazardReportAckRepository hazardReportAckRepository;

    @Mock
    private RiskAssessmentPdfService riskAssessmentPdfService;

    @Mock
    private HazardLogExcelService hazardLogExcelService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private HazardCycleService hazardCycleService;

    @BeforeEach
    void setUp() {
        hazardCycleService = new HazardCycleService(
                hazardReportRepository,
                hazardReportPhotoRepository,
                hazardReportAckRepository,
                userRepository,
                geminiService,
                storageService,
                activityLogService,
                cycleRewardService,
                riskAssessmentPdfService,
                hazardLogExcelService,
                new ObjectMapper(),
                eventPublisher
        );
    }

    @Test
    @DisplayName("createHazardCycle_성공_AI분석완료_1단계보상지급")
    void createHazardCycle_성공_AI분석완료_1단계보상지급() {
        User user = createUser(1L);
        MockMultipartFile photo = new MockMultipartFile("photo", "hazard.jpg", "image/jpeg", "img".getBytes());

        HazardCycleCreateRequest request = new HazardCycleCreateRequest();
        request.setDescription("전기 패널 주변 물기");
        request.setLocation("A동 1층");
        request.setClientTempId("temp-123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hazardReportRepository.findByClientTempId("temp-123")).thenReturn(Optional.empty());
        when(hazardReportRepository.countByReporterIdAndReportedAtBetween(eq(1L), any(), any())).thenReturn(0L);
        when(storageService.store(any())).thenReturn("hazard-1.jpg");
        when(hazardReportRepository.save(any(HazardReport.class))).thenAnswer(invocation -> {
            HazardReport report = invocation.getArgument(0);
            if (ReflectionTestUtils.getField(report, "id") == null) {
                ReflectionTestUtils.setField(report, "id", 1L);
            }
            return report;
        });

        GeminiAnalysisResult ai = new GeminiAnalysisResult();
        ai.setRiskLevel("HIGH");
        ai.setRiskFactor("누전 위험");
        ai.setRemediationSteps(List.of("전원 차단", "절연 조치"));
        ai.setReferenceCode("KOSHA-E-2023-07");
        when(geminiService.analyzeImage(any(byte[].class), anyString())).thenReturn(ai);

        when(cycleRewardService.awardTier1(any(HazardReport.class)))
                .thenReturn(new CycleRewardService.RewardGrant(100, 1200, "1단계 보상 지급 완료"));

        HazardCycleResponse response = hazardCycleService.createHazardCycle(1L, photo, request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(CycleStatus.AI_ANALYZED.name());
        assertThat(response.getAiAnalysis()).isNotNull();
        assertThat(response.getTier1Reward()).isNotNull();
        assertThat(response.getTier1Reward().getPointsAwarded()).isEqualTo(100);
    }

    @Test
    @DisplayName("createHazardCycle_오프라인중복_기존결과반환")
    void createHazardCycle_오프라인중복_기존결과반환() {
        User user = createUser(1L);

        HazardReport existing = HazardReport.builder()
                .reporter(user)
                .hazardPhotoPath("existing.jpg")
                .reportedAt(LocalDateTime.now())
                .status(CycleStatus.AI_ANALYZED)
                .clientTempId("temp-1")
                .build();
        existing.applyAiAnalysis("MEDIUM", "기존 위험", "[\"조치\"]", "KOSHA-P-2023-12", 10, LocalDateTime.now());
        ReflectionTestUtils.setField(existing, "id", 99L);

        HazardCycleCreateRequest request = new HazardCycleCreateRequest();
        request.setClientTempId("temp-1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hazardReportRepository.findByClientTempId("temp-1")).thenReturn(Optional.of(existing));

        HazardCycleResponse response = hazardCycleService.createHazardCycle(
                1L,
                new MockMultipartFile("photo", "a.jpg", "image/jpeg", "img".getBytes()),
                request);

        assertThat(response.getId()).isEqualTo(99L);
        verify(storageService, never()).store(any());
    }

    @Test
    @DisplayName("completeCycle_성공_2단계보상지급")
    void completeCycle_성공_2단계보상지급() {
        User user = createUser(1L);
        HazardReport report = HazardReport.builder()
                .reporter(user)
                .hazardPhotoPath("hazard.jpg")
                .reportedAt(LocalDateTime.now())
                .status(CycleStatus.AI_ANALYZED)
                .build();
        ReflectionTestUtils.setField(report, "id", 1L);

        when(hazardReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(storageService.store(any())).thenReturn("completion.jpg");
        when(cycleRewardService.awardTier2(any(HazardReport.class)))
                .thenReturn(new CycleRewardService.RewardGrant(100, 1300, "2단계 보상 지급 완료"));

        CompletionUploadRequest request = new CompletionUploadRequest();
        request.setNote("절연 조치 완료");

        HazardCycleResponse response = hazardCycleService.completeCycle(
                1L,
                1L,
                new MockMultipartFile("photo", "completion.jpg", "image/jpeg", "img".getBytes()),
                request);

        assertThat(response.getStatus()).isEqualTo(CycleStatus.ACTION_COMPLETED.name());
        assertThat(response.getTier2Reward()).isNotNull();
        assertThat(response.getTier2Reward().getPointsAwarded()).isEqualTo(100);
    }

    @Test
    @DisplayName("completeCycle_본인아닌경우_FORBIDDEN")
    void completeCycle_본인아닌경우_FORBIDDEN() {
        User owner = createUser(2L);
        HazardReport report = HazardReport.builder()
                .reporter(owner)
                .hazardPhotoPath("hazard.jpg")
                .reportedAt(LocalDateTime.now())
                .status(CycleStatus.AI_ANALYZED)
                .build();
        ReflectionTestUtils.setField(report, "id", 1L);

        when(hazardReportRepository.findById(1L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> hazardCycleService.completeCycle(
                1L,
                1L,
                new MockMultipartFile("photo", "completion.jpg", "image/jpeg", "img".getBytes()),
                new CompletionUploadRequest()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HAZARD_CYCLE_FORBIDDEN);
    }

    @Test
    @DisplayName("completeCycle_이미완료_예외발생")
    void completeCycle_이미완료_예외발생() {
        User user = createUser(1L);
        HazardReport report = HazardReport.builder()
                .reporter(user)
                .hazardPhotoPath("hazard.jpg")
                .reportedAt(LocalDateTime.now())
                .status(CycleStatus.AI_ANALYZED)
                .build();
        report.completeAction("done.jpg", "완료", LocalDateTime.now());
        ReflectionTestUtils.setField(report, "id", 1L);

        when(hazardReportRepository.findById(1L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> hazardCycleService.completeCycle(
                1L,
                1L,
                new MockMultipartFile("photo", "completion.jpg", "image/jpeg", "img".getBytes()),
                new CompletionUploadRequest()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.HAZARD_CYCLE_ALREADY_COMPLETED);
    }

    @Test
    @DisplayName("syncOfflineCycles_배치동기화_성공")
    void syncOfflineCycles_배치동기화_성공() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hazardReportRepository.findByClientTempId(anyString())).thenReturn(Optional.empty());
        when(hazardReportRepository.countByReporterIdAndReportedAtBetween(eq(1L), any(), any())).thenReturn(0L);
        when(storageService.store(any())).thenReturn("sync.jpg");
        when(hazardReportRepository.save(any(HazardReport.class))).thenAnswer(invocation -> {
            HazardReport report = invocation.getArgument(0);
            if (ReflectionTestUtils.getField(report, "id") == null) {
                ReflectionTestUtils.setField(report, "id", 10L);
            }
            return report;
        });

        GeminiAnalysisResult ai = new GeminiAnalysisResult();
        ai.setRiskLevel("HIGH");
        ai.setRiskFactor("감전 위험");
        ai.setRemediationSteps(List.of("전원 차단"));
        ai.setReferenceCode("KOSHA-E-2023-07");
        when(geminiService.analyzeImage(any(byte[].class), anyString())).thenReturn(ai);
        when(cycleRewardService.awardTier1(any(HazardReport.class)))
                .thenReturn(new CycleRewardService.RewardGrant(100, 1100, "1단계 보상"));

        CycleSyncRequest syncRequest = new CycleSyncRequest();
        syncRequest.setClientTempId("temp-sync-1");
        syncRequest.setDescription("오프라인 등록");
        syncRequest.setLocation("현장 A");
        syncRequest.setReportedAt(LocalDateTime.now());
        syncRequest.setPhotoFileName("offline.jpg");

        MockMultipartFile photo = new MockMultipartFile("photos", "offline.jpg", "image/jpeg", "img".getBytes());

        CycleSyncResponse response = hazardCycleService.syncOfflineCycles(1L, List.of(syncRequest), List.of(photo));

        assertThat(response.getSyncedCount()).isEqualTo(1);
        assertThat(response.getFailedCount()).isEqualTo(0);
        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResults().get(0).getError()).isNull();
    }

    @Test
    @DisplayName("getStats_통계조회_정상")
    void getStats_통계조회_정상() {
        when(hazardReportRepository.countByReporterId(1L)).thenReturn(10L);
        when(hazardReportRepository.countByReporterIdAndStatus(1L, CycleStatus.ACTION_COMPLETED)).thenReturn(4L);
        when(hazardReportRepository.sumPointsByReporterId(1L)).thenReturn(700);
        when(hazardReportRepository.countByReporterIdAndReportedAtBetween(eq(1L), any(), any())).thenReturn(3L);
        when(hazardReportRepository.countByRiskLevel(1L)).thenReturn(List.of(
                new Object[]{"HIGH", 2L},
                new Object[]{"MEDIUM", 5L}
        ));

        CycleStatsResponse stats = hazardCycleService.getStats(1L);

        assertThat(stats.getTotalCycles()).isEqualTo(10);
        assertThat(stats.getCompletedCycles()).isEqualTo(4);
        assertThat(stats.getPendingCycles()).isEqualTo(6);
        assertThat(stats.getTotalPointsEarned()).isEqualTo(700);
        assertThat(stats.getRiskLevelDistribution()).isEqualTo(Map.of("HIGH", 2L, "MEDIUM", 5L));
    }

    private User createUser(Long id) {
        User user = User.builder()
                .username("worker" + id)
                .password("pw")
                .name("작업자" + id)
                .role(Role.ROLE_WORKER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
