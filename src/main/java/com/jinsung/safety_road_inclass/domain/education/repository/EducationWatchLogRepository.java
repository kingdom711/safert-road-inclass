package com.jinsung.safety_road_inclass.domain.education.repository;

import com.jinsung.safety_road_inclass.domain.education.entity.EducationWatchLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * EducationWatchLogRepository
 */
public interface EducationWatchLogRepository extends JpaRepository<EducationWatchLog, Long> {

    /**
     * 특정 유저 + 교육ID의 전체 시청 체크포인트 조회
     */
    List<EducationWatchLog> findByUserIdAndEducationIdOrderByServerTimeAsc(Long userId, String educationId);

    /**
     * 특정 completion에 연결된 체크포인트 조회
     */
    List<EducationWatchLog> findByCompletionIdOrderByServerTimeAsc(Long completionId);
}
