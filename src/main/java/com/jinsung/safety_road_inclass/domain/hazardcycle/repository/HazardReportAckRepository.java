package com.jinsung.safety_road_inclass.domain.hazardcycle.repository;

import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReportAck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HazardReportAckRepository extends JpaRepository<HazardReportAck, Long> {

    List<HazardReportAck> findByHazardReportIdOrderByAckedAtAsc(Long hazardReportId);

    boolean existsByHazardReportIdAndAckerId(Long hazardReportId, Long ackerId);

    long countByHazardReportId(Long hazardReportId);
}
