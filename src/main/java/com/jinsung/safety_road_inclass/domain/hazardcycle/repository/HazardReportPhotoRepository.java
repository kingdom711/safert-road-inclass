package com.jinsung.safety_road_inclass.domain.hazardcycle.repository;

import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReportPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HazardReportPhotoRepository extends JpaRepository<HazardReportPhoto, Long> {

    List<HazardReportPhoto> findByHazardReportIdOrderByDisplayOrderAsc(Long hazardReportId);
}
