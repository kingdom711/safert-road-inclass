package com.jinsung.safety_road_inclass.domain.workstop.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.domain.workstop.entity.ReportStatus;
import com.jinsung.safety_road_inclass.domain.workstop.entity.WorkStopReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkStopReportRepository extends JpaRepository<WorkStopReport, Long> {

    List<WorkStopReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<WorkStopReport> findByReporterOrderByCreatedAtDesc(User reporter);

    List<WorkStopReport> findAllByOrderByCreatedAtDesc();

    /** 기간별 신고 조회 (컴플라이언스 리포트용) */
    @Query("SELECT r FROM WorkStopReport r WHERE r.createdAt BETWEEN :start AND :end ORDER BY r.createdAt DESC")
    List<WorkStopReport> findByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 미해결 건수 */
    long countByStatusNotIn(List<ReportStatus> resolvedStatuses);

    /** 기간별 위험 유형 통계 */
    @Query("SELECT r.hazardType, COUNT(r) FROM WorkStopReport r " +
           "WHERE r.createdAt BETWEEN :start AND :end GROUP BY r.hazardType")
    List<Object[]> countByHazardTypeAndPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 기간별 총 건수 */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByReporterTeamAndCreatedAtBetween(Team team, LocalDateTime start, LocalDateTime end);

    /** 기간별 해결 건수 */
    long countByCreatedAtBetweenAndStatusIn(LocalDateTime start, LocalDateTime end, List<ReportStatus> statuses);
}
