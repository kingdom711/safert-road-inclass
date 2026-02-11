package com.jinsung.safety_road_inclass.domain.gameprofile.service;

import com.jinsung.safety_road_inclass.domain.attendance.repository.UserStreakRepository;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.gameprofile.dto.*;
import com.jinsung.safety_road_inclass.domain.gameprofile.entity.UserGameProfile;
import com.jinsung.safety_road_inclass.domain.gameprofile.entity.UserSpecialization;
import com.jinsung.safety_road_inclass.domain.gameprofile.repository.GameProfileRepository;
import com.jinsung.safety_road_inclass.domain.gameprofile.repository.UserSpecializationRepository;
import com.jinsung.safety_road_inclass.domain.point.entity.UserPoints;
import com.jinsung.safety_road_inclass.domain.point.repository.UserPointsRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GameProfileService - 게임 프로필 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GameProfileService {

    private final GameProfileRepository gameProfileRepository;
    private final UserSpecializationRepository userSpecializationRepository;
    private final UserRepository userRepository;
    private final UserPointsRepository userPointsRepository;
    private final UserStreakRepository userStreakRepository;

    /**
     * 게임 프로필 조회 (없으면 생성)
     */
    @Transactional
    public GameProfileResponse getOrCreateProfile(Long userId) {
        User user = findUser(userId);
        UserGameProfile profile = gameProfileRepository.findByUserId(userId)
                .orElseGet(() -> gameProfileRepository.save(new UserGameProfile(user)));
        return GameProfileResponse.from(profile);
    }

    /**
     * 전체 게임 데이터 일괄 조회 (크로스 디바이스 동기화용)
     */
    @Transactional
    public FullGameDataResponse getFullGameData(Long userId) {
        User user = findUser(userId);

        // 게임 프로필
        UserGameProfile profile = gameProfileRepository.findByUserId(userId)
                .orElseGet(() -> gameProfileRepository.save(new UserGameProfile(user)));

        // 전직 목록
        List<SpecializationResponse> specializations = userSpecializationRepository.findByUserId(userId)
                .stream()
                .map(SpecializationResponse::from)
                .collect(Collectors.toList());

        // 포인트
        FullGameDataResponse.PointsSummary pointsSummary = userPointsRepository.findByUserId(userId)
                .map(up -> FullGameDataResponse.PointsSummary.builder()
                        .balance(up.getBalance())
                        .totalEarned(up.getTotalEarned())
                        .totalSpent(up.getTotalSpent())
                        .build())
                .orElse(FullGameDataResponse.PointsSummary.builder()
                        .balance(0).totalEarned(0).totalSpent(0).build());

        // 스트릭
        FullGameDataResponse.StreakSummary streakSummary = userStreakRepository.findByUserId(userId)
                .map(us -> FullGameDataResponse.StreakSummary.builder()
                        .currentStreak(us.getCurrentStreak())
                        .longestStreak(us.getLongestStreak())
                        .lastCheckInDate(us.getLastCheckInDate())
                        .build())
                .orElse(FullGameDataResponse.StreakSummary.builder()
                        .currentStreak(0).longestStreak(0).build());

        log.info("Full game data retrieved: userId={}, level={}", userId, profile.getLevel());

        return FullGameDataResponse.builder()
                .profile(GameProfileResponse.from(profile))
                .specializations(specializations)
                .points(pointsSummary)
                .streak(streakSummary)
                .build();
    }

    /**
     * 경험치 추가
     */
    @Transactional
    public AddExpResponse addExp(Long userId, int amount, String source) {
        User user = findUser(userId);
        UserGameProfile profile = gameProfileRepository.findByUserId(userId)
                .orElseGet(() -> gameProfileRepository.save(new UserGameProfile(user)));

        int levelsGained = profile.addExp(amount);

        log.info("Exp added: userId={}, amount={}, source={}, level={}, levelsGained={}",
                userId, amount, source, profile.getLevel(), levelsGained);

        return AddExpResponse.builder()
                .addedExp(amount)
                .currentLevel(profile.getLevel())
                .currentExp(profile.getExp())
                .expToNext(profile.getExpToNext())
                .levelsGained(levelsGained)
                .source(source)
                .build();
    }

    /**
     * 게임 역할 설정
     */
    @Transactional
    public GameProfileResponse setGameRole(Long userId, String role) {
        User user = findUser(userId);
        UserGameProfile profile = gameProfileRepository.findByUserId(userId)
                .orElseGet(() -> gameProfileRepository.save(new UserGameProfile(user)));

        profile.setGameRole(role);
        log.info("Game role set: userId={}, role={}", userId, role);

        return GameProfileResponse.from(profile);
    }

    /**
     * 해금된 전직 목록 조회
     */
    public List<SpecializationResponse> getSpecializations(Long userId) {
        return userSpecializationRepository.findByUserId(userId)
                .stream()
                .map(SpecializationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 전직 해금
     */
    @Transactional
    public SpecializationResponse unlockSpecialization(Long userId, String specId, String educationProgress) {
        User user = findUser(userId);

        // 이미 해금되었는지 확인
        if (userSpecializationRepository.findByUserIdAndSpecId(userId, specId).isPresent()) {
            throw new CustomException(ErrorCode.GAMEPROFILE_SPECIALIZATION_ALREADY_UNLOCKED);
        }

        UserSpecialization spec = UserSpecialization.builder()
                .user(user)
                .specId(specId)
                .unlockedAt(LocalDateTime.now())
                .educationProgress(educationProgress)
                .build();

        userSpecializationRepository.save(spec);
        log.info("Specialization unlocked: userId={}, specId={}", userId, specId);

        return SpecializationResponse.from(spec);
    }

    /**
     * 활성 전직 변경
     */
    @Transactional
    public GameProfileResponse setActiveSpecialization(Long userId, String specId) {
        User user = findUser(userId);
        UserGameProfile profile = gameProfileRepository.findByUserId(userId)
                .orElseGet(() -> gameProfileRepository.save(new UserGameProfile(user)));

        // 해금된 전직인지 확인
        userSpecializationRepository.findByUserIdAndSpecId(userId, specId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAMEPROFILE_SPECIALIZATION_NOT_FOUND));

        profile.setActiveSpecialization(specId);
        log.info("Active specialization set: userId={}, specId={}", userId, specId);

        return GameProfileResponse.from(profile);
    }

    /**
     * 게임 프로필 업데이트 (PUT)
     */
    @Transactional
    public GameProfileResponse updateProfile(Long userId, String gameRole, String activeSpecialization) {
        User user = findUser(userId);
        UserGameProfile profile = gameProfileRepository.findByUserId(userId)
                .orElseGet(() -> gameProfileRepository.save(new UserGameProfile(user)));

        if (gameRole != null) {
            profile.setGameRole(gameRole);
        }
        if (activeSpecialization != null) {
            profile.setActiveSpecialization(activeSpecialization);
        }

        return GameProfileResponse.from(profile);
    }

    /**
     * localStorage → 서버 마이그레이션
     */
    @Transactional
    public FullGameDataResponse syncFromLocalStorage(Long userId, GameProfileSyncRequest request) {
        User user = findUser(userId);

        // 1. 게임 프로필 동기화
        UserGameProfile profile = gameProfileRepository.findByUserId(userId)
                .orElseGet(() -> gameProfileRepository.save(new UserGameProfile(user)));

        profile.syncFromData(
                request.getLevel(),
                request.getExp(),
                request.getExpToNext(),
                request.getGameRole(),
                request.getActiveSpecialization(),
                request.getTotalQuestsCompleted());

        // 2. 전직 데이터 동기화
        if (request.getSpecializations() != null) {
            for (GameProfileSyncRequest.SpecializationData specData : request.getSpecializations()) {
                if (userSpecializationRepository.findByUserIdAndSpecId(userId, specData.getSpecId()).isEmpty()) {
                    LocalDateTime unlockedAt = specData.getUnlockedAt() != null
                            ? LocalDateTime.parse(specData.getUnlockedAt())
                            : LocalDateTime.now();

                    UserSpecialization spec = UserSpecialization.builder()
                            .user(user)
                            .specId(specData.getSpecId())
                            .unlockedAt(unlockedAt)
                            .educationProgress(specData.getEducationProgress())
                            .build();
                    userSpecializationRepository.save(spec);
                }
            }
        }

        // 3. 포인트 동기화 (서버에 없으면 생성)
        if (request.getPointsBalance() > 0) {
            UserPoints userPoints = userPointsRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        UserPoints up = new UserPoints(user);
                        return userPointsRepository.save(up);
                    });
            // 서버의 포인트가 0이면 localStorage 값으로 설정
            if (userPoints.getBalance() == 0 && request.getPointsBalance() > 0) {
                userPoints.earn(request.getPointsBalance());
            }
        }

        log.info("Game data synced from localStorage: userId={}, level={}", userId, request.getLevel());

        return getFullGameData(userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
