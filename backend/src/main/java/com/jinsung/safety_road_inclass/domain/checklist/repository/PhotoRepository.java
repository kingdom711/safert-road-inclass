package com.jinsung.safety_road_inclass.domain.checklist.repository;

import com.jinsung.safety_road_inclass.domain.checklist.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * PhotoRepository - 사진 데이터 접근
 */
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByChecklistId(Long checklistId);

    void deleteByChecklistId(Long checklistId);
}

