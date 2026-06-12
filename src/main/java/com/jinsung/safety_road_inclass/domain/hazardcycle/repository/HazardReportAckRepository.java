package com.jinsung.safety_road_inclass.domain.hazardcycle.repository;

import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReportAck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HazardReportAckRepository extends JpaRepository<HazardReportAck, Long> {

    List<HazardReportAck> findByHazardReportIdOrderByAckedAtAsc(Long hazardReportId);

    boolean existsByHazardReportIdAndAckerId(Long hazardReportId, Long ackerId);

    long countByHazardReportId(Long hazardReportId);

    @Query("SELECT a.acker.id, COUNT(a), MAX(a.ackedAt) FROM HazardReportAck a " +
            "WHERE a.ackedAt >= :start AND a.ackedAt < :end GROUP BY a.acker.id")
    List<Object[]> aggregateByAckerAndAckedAtBetween(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);
}
