package com.jinsung.safety_road_inclass.domain.activity.service;

import com.jinsung.safety_road_inclass.domain.activity.dto.ActivityLogResponse;
import com.jinsung.safety_road_inclass.domain.activity.entity.ActivityLog;
import com.jinsung.safety_road_inclass.domain.activity.entity.ActivityType;
import com.jinsung.safety_road_inclass.domain.activity.repository.ActivityLogRepository;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ActivityLogService - 활동 기록 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    /**
     * 활동 기록 저장
     */
    @Transactional
    public void log(Long userId, ActivityType activityType, String title, String description, String metadata) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ActivityLog activityLog = ActivityLog.builder()
                .user(user)
                .activityType(activityType)
                .title(title)
                .description(description)
                .metadata(metadata)
                .build();

        activityLogRepository.save(activityLog);
        log.info("Activity logged: userId={}, type={}, title={}", userId, activityType, title);
    }

    /**
     * 유저별 활동 기록 페이징 조회
     */
    public Page<ActivityLogResponse> getActivities(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(ActivityLogResponse::from);
    }
}
