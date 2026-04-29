package com.jinsung.safety_road_inclass.domain.alert.repository;

import com.jinsung.safety_road_inclass.domain.alert.entity.Alert;
import com.jinsung.safety_road_inclass.domain.alert.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AlertRepository - 알림 데이터 접근
 */
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * 모든 알림 조회 (우선순위 높은 순, 최신순)
     */
    List<Alert> findAllByOrderByPriorityDescCreatedAtDesc();

    /**
     * 활성화된 알림 조회 (현재 시간 기준)
     * - active = true
     * - startDate가 null이거나 현재 시간 이전
     * - endDate가 null이거나 현재 시간 이후
     */
    @Query("SELECT a FROM Alert a " +
           "WHERE a.active = true " +
           "AND (a.startDate IS NULL OR a.startDate <= :now) " +
           "AND (a.endDate IS NULL OR a.endDate >= :now) " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Alert> findActiveAlerts(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Alert a " +
           "WHERE a.active = true " +
           "AND (a.recipient IS NULL OR a.recipient.id = :userId) " +
           "AND (a.startDate IS NULL OR a.startDate <= :now) " +
           "AND (a.endDate IS NULL OR a.endDate >= :now) " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Alert> findActiveAlertsForUser(@Param("now") LocalDateTime now, @Param("userId") Long userId);

    /**
     * 알림 유형별 조회
     */
    List<Alert> findByTypeOrderByPriorityDescCreatedAtDesc(AlertType type);

    /**
     * 작성자별 알림 조회
     */
    List<Alert> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
}
