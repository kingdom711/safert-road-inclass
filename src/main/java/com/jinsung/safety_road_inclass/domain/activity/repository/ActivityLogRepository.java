package com.jinsung.safety_road_inclass.domain.activity.repository;

import com.jinsung.safety_road_inclass.domain.activity.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ActivityLogRepository - 활동 기록 데이터 접근
 */
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * 유저별 활동 기록 페이징 조회 (최신순)
     */
    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
