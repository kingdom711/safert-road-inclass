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

    long countByReportedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusIn(List<CycleStatus> statuses);

    Page<HazardReport> findAllByOrderByReportedAtDesc(Pageable pageable);

    @Query("SELECT h FROM HazardReport h JOIN FETCH h.reporter WHERE h.status IN :statuses ORDER BY h.reportedAt DESC")
    List<HazardReport> findByStatusInOrderByReportedAtDesc(@Param("statuses") List<CycleStatus> statuses);

    @Query("SELECT h.aiRiskLevel, COUNT(h) FROM HazardReport h " +
            "WHERE h.reporter.id = :reporterId AND h.aiRiskLevel IS NOT NULL GROUP BY h.aiRiskLevel")
    List<Object[]> countByRiskLevel(@Param("reporterId") Long reporterId);

    long countByReporterIdAndReportedAtBetween(
            Long reporterId,
            LocalDateTime start,
            LocalDateTime end);

    long countByReporterIdAndReportedAtGreaterThanEqualAndReportedAtLessThan(
            Long reporterId,
            LocalDateTime start,
            LocalDateTime end);

    long countByReporterIdAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
            Long reporterId,
            LocalDateTime start,
            LocalDateTime end);

    @Query("SELECT COALESCE(SUM(h.tier1PointsAwarded + h.tier2PointsAwarded), 0) FROM HazardReport h WHERE h.reporter.id = :reporterId")
    Integer sumPointsByReporterId(@Param("reporterId") Long reporterId);

    /**
     * 동일 사진 해시의 최근 분석 완료 사이클 조회 (캐시 히트 판정용).
     * analyzedAfter 이후에 분석된 결과만 유효한 캐시로 간주한다.
     */
    @Query("SELECT h FROM HazardReport h " +
            "WHERE h.photoSha256 = :sha256 " +
            "AND h.aiAnalyzedAt IS NOT NULL " +
            "AND h.aiAnalyzedAt >= :analyzedAfter " +
            "ORDER BY h.aiAnalyzedAt DESC")
    List<HazardReport> findRecentAnalyzedByHash(
            @Param("sha256") String sha256,
            @Param("analyzedAfter") LocalDateTime analyzedAfter);

    /**
     * 월간 위험요인 개선대장용 — 해당 월에 보고된 사이클 전체.
     */
    List<HazardReport> findByReportedAtBetweenOrderByReportedAtAsc(
            LocalDateTime start,
            LocalDateTime end);

    Optional<HazardReport> findByCertNumber(String certNumber);

    /**
     * 해시체인 이전 링크용 — 가장 최근 봉인된(integrity_hash IS NOT NULL) 사이클 1건.
     */
    @Query("SELECT h FROM HazardReport h " +
            "WHERE h.integrityHash IS NOT NULL " +
            "ORDER BY h.id DESC")
    List<HazardReport> findMostRecentSealed(org.springframework.data.domain.Pageable pageable);
}
