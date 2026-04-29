package com.jinsung.safety_road_inclass.domain.education.service;

import com.jinsung.safety_road_inclass.domain.activity.entity.ActivityType;
import com.jinsung.safety_road_inclass.domain.activity.service.ActivityLogService;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.education.dto.*;
import com.jinsung.safety_road_inclass.domain.education.entity.EducationCompletion;
import com.jinsung.safety_road_inclass.domain.education.entity.EducationWatchLog;
import com.jinsung.safety_road_inclass.domain.education.entity.QuizAttemptLog;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationCompletionRepository;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationWatchLogRepository;
import com.jinsung.safety_road_inclass.domain.education.repository.QuizAttemptLogRepository;
import com.jinsung.safety_road_inclass.domain.quest.event.EducationCompletedEvent;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * EducationService - 교육 수료 기록 서비스
 *
 * 핵심 기능:
 * 1. 영상 시청 체크포인트 저장 (30초 간격)
 * 2. 퀴즈 답안 상세 저장
 * 3. 교육 완료 최종 기록 + SHA-256 해시 체인 생성
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EducationService {

    private final UserRepository                userRepository;
    private final EducationCompletionRepository completionRepository;
    private final EducationWatchLogRepository   watchLogRepository;
    private final QuizAttemptLogRepository      quizAttemptLogRepository;
    private final HashChainService              hashChainService;
    private final ActivityLogService            activityLogService;
    private final ApplicationEventPublisher     eventPublisher;

    // ─── 1. 영상 시청 체크포인트 ─────────────────────────────────────

    /**
     * 시청 체크포인트 저장
     * 프론트엔드가 30초 간격으로 호출
     */
    @Transactional
    public WatchCheckpointResponse saveCheckpoint(Long userId, WatchCheckpointRequest req) {
        User user = findUser(userId);

        EducationWatchLog log = EducationWatchLog.builder()
                .user(user)
                .educationId(req.getEducationId())
                .checkpointSec(req.getCheckpointSec())
                .totalSec(req.getTotalSec())
                .isPlaying(req.getIsPlaying())
                .tabVisible(req.getTabVisible())
                .build();

        EducationWatchLog saved = watchLogRepository.save(log);

        return WatchCheckpointResponse.builder()
                .logId(saved.getId())
                .serverTime(saved.getServerTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .checkpointSec(saved.getCheckpointSec())
                .build();
    }

    // ─── 2. 퀴즈 답안 저장 ───────────────────────────────────────────

    /**
     * 퀴즈 제출 시 개별 답안 저장
     * correctAnswer 검증은 교육 데이터 기준으로 수행하지 않고
     * (프론트에서 처리), 이 메서드는 답안 이력 보존에만 집중
     */
    @Transactional
    public QuizSubmitResponse saveQuizAnswers(Long userId, QuizSubmitRequest req) {
        User user = findUser(userId);

        List<QuizAttemptLog> logs = req.getAnswers().stream()
                .map(answer -> QuizAttemptLog.builder()
                        .user(user)
                        .educationId(req.getEducationId())
                        .attemptNumber(req.getAttemptNumber())
                        .questionId(answer.getQuestionId())
                        .selectedOption(answer.getSelectedOption())
                        .correct(false)   // Phase 2에서 서버 정답 검증으로 교체
                        .timeSpentSec(answer.getTimeSpentSec())
                        .build())
                .toList();

        quizAttemptLogRepository.saveAll(logs);

        String savedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        log.info("[Education] Quiz answers saved: userId={}, educationId={}, attempt={}, count={}",
                userId, req.getEducationId(), req.getAttemptNumber(), logs.size());

        return QuizSubmitResponse.builder()
                .savedCount(logs.size())
                .attemptNumber(req.getAttemptNumber())
                .savedAt(savedAt)
                .build();
    }

    // ─── 3. 교육 완료 최종 기록 ──────────────────────────────────────

    /**
     * 교육 완료 최종 기록 + 해시 체인 생성
     *
     * @param userId    인증된 유저 ID
     * @param req       클라이언트 완료 데이터
     * @param ipAddress HTTP 요청 IP
     * @param userAgent HTTP User-Agent
     */
    @Transactional
    public EducationCompleteResponse complete(Long userId,
                                              EducationCompleteRequest req,
                                              String ipAddress,
                                              String userAgent) {
        User user = findUser(userId);

        // 1. 시작 시각 파싱 (클라이언트 제출값)
        LocalDateTime startedAt = LocalDateTime.parse(req.getStartedAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 2. 수료 레코드 저장 (해시는 ID 확정 후 채움)
        EducationCompletion completion = EducationCompletion.builder()
                .user(user)
                .educationId(req.getEducationId())
                .educationTitle(req.getEducationTitle())
                .educationType(req.getEducationType())
                .startedAt(startedAt)
                .completedAt(LocalDateTime.now())   // 서버 시각으로 기록
                .videoWatchSeconds(req.getVideoWatchSeconds())
                .videoTotalSeconds(req.getVideoTotalSeconds())
                .videoWatchRatio(req.getVideoWatchRatio())
                .quizScore(req.getQuizScore())
                .quizPassed(req.getQuizPassed())
                .attemptCount(req.getAttemptCount())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceHash(req.getDeviceHash())
                .build();

        completionRepository.save(completion);

        // 3. 이전 레코드 해시 조회 (체이닝)
        String prevHash = completionRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .filter(prev -> !prev.getId().equals(completion.getId()))
                .map(EducationCompletion::getIntegrityHash)
                .orElse(null);

        // 4. 무결성 해시 + 수료증 번호 생성
        String integrityHash = hashChainService.generateHash(completion, prevHash);
        String certNumber    = hashChainService.generateCertNumber();

        completion.applyHashChain(certNumber, integrityHash, prevHash);

        // 5. 이전에 저장된 시청 로그와 quiz 로그에 completion_id 연결은
        //    별도 배치 또는 조회 방식으로 처리 (Phase 2 개선 예정)

        // 6. 활동 기록 저장
        String activityDesc = String.format("%s 수료 완료 (점수: %d점, 시청률: %.0f%%)",
                req.getEducationTitle(), req.getQuizScore(),
                req.getVideoWatchRatio().doubleValue() * 100);

        ActivityType activityType = req.getQuizPassed()
                ? ActivityType.EDUCATION_QUIZ_PASSED
                : ActivityType.EDUCATION_COMPLETED;

        activityLogService.log(userId, activityType, "교육 수료", activityDesc,
                String.format("{\"certNumber\":\"%s\",\"educationId\":\"%s\"}",
                        certNumber, req.getEducationId()));

        log.info("[Education] Complete: userId={}, educationId={}, cert={}, score={}, hash={}",
                userId, req.getEducationId(), certNumber, req.getQuizScore(),
                integrityHash.substring(0, 8) + "...");

        eventPublisher.publishEvent(new EducationCompletedEvent(
                userId,
                req.getEducationId(),
                completion.getCompletedAt()
        ));

        return EducationCompleteResponse.from(completion);
    }

    // ─── 내부 유틸 ───────────────────────────────────────────────────

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
