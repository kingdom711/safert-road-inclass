package com.jinsung.safety_road_inclass.domain.compliance.repository;

import com.jinsung.safety_road_inclass.domain.compliance.entity.ComplianceReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, Long> {

    /** 특정 연월 리포트 조회 */
    Optional<ComplianceReport> findByReportYearAndReportMonth(Integer year, Integer month);

    /** 특정 연도 리포트 목록 */
    List<ComplianceReport> findByReportYearOrderByReportMonthDesc(Integer year);

    /** 전체 리포트 최신순 */
    List<ComplianceReport> findAllByOrderByReportYearDescReportMonthDesc();

    /** 해당 연월 리포트 존재 여부 */
    boolean existsByReportYearAndReportMonth(Integer year, Integer month);
}
