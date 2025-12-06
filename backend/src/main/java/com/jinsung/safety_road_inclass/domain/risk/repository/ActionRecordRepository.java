package com.jinsung.safety_road_inclass.domain.risk.repository;

import com.jinsung.safety_road_inclass.domain.risk.entity.ActionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ActionRecordRepository - 조치 기록 데이터 접근
 */
public interface ActionRecordRepository extends JpaRepository<ActionRecord, Long> {

    /**
     * 대책별 조치 기록 조회
     */
    List<ActionRecord> findByCountermeasureIdOrderByCreatedAtDesc(Long countermeasureId);
}

