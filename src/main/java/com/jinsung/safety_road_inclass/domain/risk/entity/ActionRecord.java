package com.jinsung.safety_road_inclass.domain.risk.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ActionRecord - 조치 기록 Entity
 * 대책 수립 후 실제 조치한 내용과 전후 사진을 기록
 */
@Entity
@Table(name = "action_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActionRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "countermeasure_id", nullable = false)
    private Countermeasure countermeasure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(length = 500)
    private String actionContent;

    @Column(length = 500)
    private String beforePhotoPath;

    @Column(length = 500)
    private String afterPhotoPath;

    @Builder
    public ActionRecord(User createdBy, String actionContent, 
                       String beforePhotoPath, String afterPhotoPath) {
        this.createdBy = createdBy;
        this.actionContent = actionContent;
        this.beforePhotoPath = beforePhotoPath;
        this.afterPhotoPath = afterPhotoPath;
    }

    void setCountermeasure(Countermeasure countermeasure) {
        this.countermeasure = countermeasure;
    }
}

