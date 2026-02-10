package com.jinsung.safety_road_inclass.domain.attendance.service;

import com.jinsung.safety_road_inclass.domain.activity.entity.ActivityType;
import com.jinsung.safety_road_inclass.domain.activity.service.ActivityLogService;
import com.jinsung.safety_road_inclass.domain.attendance.dto.AttendanceHistoryResponse;
import com.jinsung.safety_road_inclass.domain.attendance.dto.CheckInResponse;
import com.jinsung.safety_road_inclass.domain.attendance.dto.StreakResponse;
import com.jinsung.safety_road_inclass.domain.attendance.entity.AttendanceRecord;
import com.jinsung.safety_road_inclass.domain.attendance.entity.UserStreak;
import com.jinsung.safety_road_inclass.domain.attendance.repository.AttendanceRecordRepository;
import com.jinsung.safety_road_inclass.domain.attendance.repository.UserStreakRepository;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.point.service.PointService;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * AttendanceService - 출석/스트릭 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private static final int BASE_POINTS = 10;
    private static final int BONUS_POINTS = 5;
    private static final int BONUS_STREAK_THRESHOLD = 7;

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserStreakRepository userStreakRepository;
    private final UserRepository userRepository;
    private final PointService pointService;
    private final ActivityLogService activityLogService;

    /**
     * 출석 체크인
     */
    @Transactional
    public CheckInResponse checkIn(Long userId) {
        LocalDate today = LocalDate.now();

        // 1. 오늘 이미 출석했는지 확인
        if (attendanceRecordRepository.existsByUserIdAndCheckInDate(userId, today)) {
            throw new CustomException(ErrorCode.ATTENDANCE_ALREADY_CHECKED_IN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. UserStreak 조회 (없으면 생성)
        UserStreak userStreak = userStreakRepository.findByUserId(userId)
                .orElseGet(() -> userStreakRepository.save(new UserStreak(user)));

        // 3. 연속 출석 계산
        userStreak.checkIn(today);

        // 4. 포인트 계산: 기본 10P + 7일 연속 보너스 5P
        int points = BASE_POINTS;
        boolean bonusAwarded = false;
        if (userStreak.getCurrentStreak() % BONUS_STREAK_THRESHOLD == 0) {
            points += BONUS_POINTS;
            bonusAwarded = true;
        }

        // 5. AttendanceRecord 저장
        AttendanceRecord record = AttendanceRecord.builder()
                .user(user)
                .checkInDate(today)
                .pointsEarned(points)
                .streakAtTime(userStreak.getCurrentStreak())
                .build();
        attendanceRecordRepository.save(record);

        // 6. PointService.addPoints() 호출
        String reason = "출석 체크인";
        String description = bonusAwarded
                ? String.format("출석 %d일차 (기본 %dP + 연속 보너스 %dP)", userStreak.getCurrentStreak(), BASE_POINTS, BONUS_POINTS)
                : String.format("출석 %d일차", userStreak.getCurrentStreak());
        pointService.addPoints(userId, points, reason, description);

        // 7. ActivityLogService.log() 호출
        activityLogService.log(userId, ActivityType.ATTENDANCE_CHECK_IN,
                "출석 체크인", description, null);

        log.info("Check-in: userId={}, streak={}, points={}, bonus={}", userId, userStreak.getCurrentStreak(), points, bonusAwarded);

        return CheckInResponse.builder()
                .checkInDate(today)
                .pointsEarned(points)
                .currentStreak(userStreak.getCurrentStreak())
                .longestStreak(userStreak.getLongestStreak())
                .bonusAwarded(bonusAwarded)
                .build();
    }

    /**
     * 스트릭 조회
     */
    public StreakResponse getStreak(Long userId) {
        return userStreakRepository.findByUserId(userId)
                .map(StreakResponse::from)
                .orElse(StreakResponse.empty());
    }

    /**
     * 출석 달력 데이터 조회
     */
    public List<AttendanceHistoryResponse> getHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        return attendanceRecordRepository
                .findByUserIdAndCheckInDateBetweenOrderByCheckInDateAsc(userId, startDate, endDate)
                .stream()
                .map(AttendanceHistoryResponse::from)
                .toList();
    }
}
