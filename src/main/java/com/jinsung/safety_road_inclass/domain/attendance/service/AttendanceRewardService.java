package com.jinsung.safety_road_inclass.domain.attendance.service;

import com.jinsung.safety_road_inclass.domain.attendance.dto.AvailableRewardResponse;
import com.jinsung.safety_road_inclass.domain.attendance.dto.RewardClaimedResponse;
import com.jinsung.safety_road_inclass.domain.attendance.entity.AttendanceRecord;
import com.jinsung.safety_road_inclass.domain.attendance.entity.AttendanceReward;
import com.jinsung.safety_road_inclass.domain.attendance.entity.AttendanceRewardType;
import com.jinsung.safety_road_inclass.domain.attendance.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.attendance.entity.UserAttendanceReward;
import com.jinsung.safety_road_inclass.domain.attendance.repository.AttendanceRewardRepository;
import com.jinsung.safety_road_inclass.domain.attendance.repository.UserAttendanceRewardRepository;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.inventory.service.InventoryService;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * AttendanceRewardService - 출석 보상 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AttendanceRewardService {

    private final AttendanceRewardRepository attendanceRewardRepository;
    private final UserAttendanceRewardRepository userAttendanceRewardRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    /**
     * 출석 보상 수령 가능 상태 생성
     */
    @Transactional
    public void createAvailableReward(Long userId, int dayNumber, LocalDate checkInDate, AttendanceRecord attendanceRecord) {
        // 1. 해당 일차의 보상 정의 조회
        AttendanceReward reward = attendanceRewardRepository
                .findByDayNumberAndActiveTrue(dayNumber)
                .orElse(null);

        if (reward == null) {
            log.debug("Day {} 보상이 정의되지 않았습니다.", dayNumber);
            return;
        }

        // 2. 이미 수령 가능 상태인지 확인
        int year = checkInDate.getYear();
        int month = checkInDate.getMonthValue();

        boolean alreadyExists = userAttendanceRewardRepository
                .findByUserIdAndAttendanceRewardIdAndYearAndMonth(userId, reward.getId(), year, month)
                .isPresent();

        if (alreadyExists) {
            log.debug("이미 수령 가능한 보상입니다: userId={}, day={}, year={}, month={}", userId, dayNumber, year, month);
            return;
        }

        // 3. UserAttendanceReward 생성
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserAttendanceReward userReward = UserAttendanceReward.builder()
                .user(user)
                .attendanceReward(reward)
                .attendanceRecord(attendanceRecord)
                .year(year)
                .month(month)
                .status(RewardStatus.AVAILABLE)
                .build();

        userAttendanceRewardRepository.save(userReward);

        log.info("출석 보상 수령 가능 상태 생성: userId={}, day={}, rewardType={}", userId, dayNumber, reward.getRewardType());
    }

    /**
     * 수령 가능한 보상 목록 조회
     */
    public List<AvailableRewardResponse> getAvailableRewards(Long userId, int year, int month) {
        return userAttendanceRewardRepository
                .findByUserIdAndYearAndMonthAndStatusOrderByCreatedAtDesc(userId, year, month, RewardStatus.AVAILABLE)
                .stream()
                .map(AvailableRewardResponse::from)
                .toList();
    }

    /**
     * 보상 수령 처리
     */
    @Transactional
    public RewardClaimedResponse claimReward(Long userId, Long rewardId) {
        // 1. UserAttendanceReward 조회 및 검증
        UserAttendanceReward userReward = userAttendanceRewardRepository.findById(rewardId)
                .orElseThrow(() -> new CustomException(ErrorCode.REWARD_NOT_FOUND));

        if (!userReward.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        if (userReward.getStatus() != RewardStatus.AVAILABLE) {
            throw new CustomException(ErrorCode.REWARD_ALREADY_CLAIMED);
        }

        AttendanceReward reward = userReward.getAttendanceReward();

        // 2. 보상 타입에 따라 처리
        Long inventoryItemId = null;

        if (reward.getRewardType() == AttendanceRewardType.ITEM_BOX && reward.getBox() != null) {
            // 아이템 상자를 인벤토리에 추가
            inventoryItemId = inventoryService.addBoxToInventory(
                    userId,
                    reward.getBox().getId(),
                    "출석",
                    userReward.getId()
            );
        } else if (reward.getRewardType() == AttendanceRewardType.PERFECT_ATTENDANCE && reward.getBox() != null) {
            // 만근 대보상: 상자 + 포인트
            inventoryItemId = inventoryService.addBoxToInventory(
                    userId,
                    reward.getBox().getId(),
                    "만근",
                    userReward.getId()
            );
            // 포인트는 별도로 지급 (AttendanceService에서 처리)
        }

        // 3. 상태를 CLAIMED로 변경
        userReward.claim();
        userAttendanceRewardRepository.save(userReward);

        log.info("보상 수령 완료: userId={}, rewardId={}, rewardType={}", userId, rewardId, reward.getRewardType());

        return RewardClaimedResponse.of(rewardId, inventoryItemId);
    }
}
