package com.jinsung.safety_road_inclass.domain.alert.service;

import com.jinsung.safety_road_inclass.domain.alert.dto.AlertRequest;
import com.jinsung.safety_road_inclass.domain.alert.dto.AlertResponse;
import com.jinsung.safety_road_inclass.domain.alert.entity.Alert;
import com.jinsung.safety_road_inclass.domain.alert.repository.AlertRepository;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AlertService - 알림 비즈니스 로직
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;

    /**
     * 모든 알림 조회
     */
    public List<AlertResponse> getAllAlerts() {
        return alertRepository.findAllByOrderByPriorityDescCreatedAtDesc().stream()
                .map(AlertResponse::from)
                .toList();
    }

    /**
     * 활성화된 알림만 조회 (현재 시간 기준)
     */
    public List<AlertResponse> getActiveAlerts() {
        return alertRepository.findActiveAlerts(LocalDateTime.now()).stream()
                .map(AlertResponse::from)
                .toList();
    }

    /**
     * 알림 상세 조회
     */
    public AlertResponse getAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));
        return AlertResponse.from(alert);
    }

    /**
     * 알림 생성
     */
    @Transactional
    public AlertResponse createAlert(AlertRequest request, User createdBy) {
        Alert alert = Alert.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .priority(request.getPriority())
                .active(request.getActive())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(createdBy)
                .build();

        Alert saved = alertRepository.save(alert);
        log.info("알림 생성: id={}, title={}, createdBy={}", 
                 saved.getId(), saved.getTitle(), createdBy.getUsername());

        return AlertResponse.from(saved);
    }

    /**
     * 알림 수정
     */
    @Transactional
    public AlertResponse updateAlert(Long id, AlertRequest request) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));

        alert.update(
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getPriority(),
                request.getActive(),
                request.getStartDate(),
                request.getEndDate()
        );

        log.info("알림 수정: id={}, title={}", alert.getId(), alert.getTitle());

        return AlertResponse.from(alert);
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));

        alertRepository.delete(alert);
        log.info("알림 삭제: id={}, title={}", id, alert.getTitle());
    }
}
