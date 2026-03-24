package com.jinsung.safety_road_inclass.domain.workstop.repository;

import com.jinsung.safety_road_inclass.domain.workstop.entity.RetaliationReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RetaliationReportRepository extends JpaRepository<RetaliationReport, Long> {

    List<RetaliationReport> findByWorkerIdOrderByCreatedAtDesc(Long workerId);

    long countByCreatedAtAfter(LocalDateTime since);
}
