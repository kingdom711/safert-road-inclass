package com.jinsung.safety_road_inclass.domain.hazardcycle.repository;

import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.CycleStatus;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HazardReportRepository extends JpaRepository<HazardReport, Long> {

    Page<HazardReport> findByReporterIdOrderByReportedAtDesc(Long reporterId, Pageable pageable);

    Page<HazardReport> findByReporterIdAndStatusOrderByReportedAtDesc(
            Long reporterId,
            CycleStatus status,
            Pageable pageable);

    Optional<HazardReport> findByClientTempId(String clientTempId);

    long countByReporterId(Long reporterId);

    long countByReporterIdAndStatus(Long reporterId, CycleStatus status);

    @Query("SELECT h.aiRiskLevel, COUNT(h) FROM HazardReport h " +
            "WHERE h.reporter.id = :reporterId AND h.aiRiskLevel IS NOT NULL GROUP BY h.aiRiskLevel")
    List<Object[]> countByRiskLevel(@Param("reporterId") Long reporterId);

    long countByReporterIdAndReportedAtBetween(
            Long reporterId,
            LocalDateTime start,
            LocalDateTime end);

    @Query("SELECT COALESCE(SUM(h.tier1PointsAwarded + h.tier2PointsAwarded), 0) FROM HazardReport h WHERE h.reporter.id = :reporterId")
    Integer sumPointsByReporterId(@Param("reporterId") Long reporterId);
}
