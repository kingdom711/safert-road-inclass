package com.jinsung.safety_road_inclass.domain.checklist.entity;

import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Photo - 사진 Entity
 */
@Entity
@Table(name = "photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Column(length = 255)
    private String originalName;

    @Column(nullable = false, length = 500)
    private String storedPath;

    @Column(length = 100)
    private String contentType;

    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PhotoType photoType;

    @Builder
    public Photo(String originalName, String storedPath, 
                 String contentType, Long fileSize, PhotoType photoType) {
        this.originalName = originalName;
        this.storedPath = storedPath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.photoType = photoType != null ? photoType : PhotoType.EVIDENCE;
    }

    void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }

    public String getUrl() {
        return "/api/v1/files/" + this.storedPath;
    }
}

