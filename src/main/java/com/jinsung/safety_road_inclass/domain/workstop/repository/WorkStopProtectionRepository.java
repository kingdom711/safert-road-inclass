package com.jinsung.safety_road_inclass.domain.workstop.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.workstop.entity.WorkStopProtection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkStopProtectionRepository extends JpaRepository<WorkStopProtection, Long> {

    /** 특정 근로자의 활성 보호 기간 조회 */
    @Query("SELECT p FROM WorkStopProtection p WHERE p.worker = :worker AND p.isActive = true AND p.endDate > :now")
    List<WorkStopProtection> findActiveProtections(@Param("worker") User worker, @Param("now") LocalDateTime now);

    /** 전체 활성 보호 건수 */
    @Query("SELECT COUNT(p) FROM WorkStopProtection p WHERE p.isActive = true AND p.endDate > :now")
    long countActiveProtections(@Param("now") LocalDateTime now);
}
