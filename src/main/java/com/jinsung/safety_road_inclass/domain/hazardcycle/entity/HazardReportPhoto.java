package com.jinsung.safety_road_inclass.domain.hazardcycle.entity;

import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * HazardReportPhoto - 사이클 다중 사진 엔티티
 */
@Entity
@Table(name = "hazard_report_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HazardReportPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hazard_report_id", nullable = false)
    private HazardReport hazardReport;

    @Enumerated(EnumType.STRING)
    @Column(name = "photo_stage", nullable = false, length = 20)
    private HazardPhotoStage photoStage;

    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    public HazardReportPhoto(HazardPhotoStage photoStage,
                             String storedPath,
                             String originalName,
                             String contentType,
                             Long fileSize,
                             Integer displayOrder) {
        this.photoStage = photoStage;
        this.storedPath = storedPath;
        this.originalName = originalName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    void setHazardReport(HazardReport hazardReport) {
        this.hazardReport = hazardReport;
    }
}
