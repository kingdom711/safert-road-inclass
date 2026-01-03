package com.jinsung.safety_road_inclass.domain.template.repository;

import com.jinsung.safety_road_inclass.domain.template.entity.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * WorkTypeRepository - 작업 유형 데이터 접근
 */
public interface WorkTypeRepository extends JpaRepository<WorkType, Long> {

    Optional<WorkType> findByName(String name);

    boolean existsByName(String name);
}

