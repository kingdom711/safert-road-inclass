package com.jinsung.safety_road_inclass.domain.checklist.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.template.entity.ChecklistTemplate;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Checklist - 체크리스트 Entity
 */
@Entity
@Table(name = "checklists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Checklist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ChecklistTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 100)
    private String siteName;

    @Column(nullable = false)
    private LocalDate workDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChecklistStatus status = ChecklistStatus.DRAFT;

    @Column(length = 500)
    private String remarks;

    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChecklistItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    @Builder
    public Checklist(ChecklistTemplate template, User createdBy, 
                     String siteName, LocalDate workDate, String remarks) {
        this.template = template;
        this.createdBy = createdBy;
        this.siteName = siteName;
        this.workDate = workDate;
        this.remarks = remarks;
    }

    // 비즈니스 메서드: 제출
    public void submit() {
        if (this.status != ChecklistStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태에서만 제출 가능합니다.");
        }
        this.status = ChecklistStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드: 승인
    public void approve() {
        if (this.status != ChecklistStatus.SUBMITTED) {
            throw new IllegalStateException("SUBMITTED 상태에서만 승인 가능합니다.");
        }
        this.status = ChecklistStatus.APPROVED;
    }

    // 비즈니스 메서드: 반려
    public void reject() {
        if (this.status != ChecklistStatus.SUBMITTED) {
            throw new IllegalStateException("SUBMITTED 상태에서만 반려 가능합니다.");
        }
        this.status = ChecklistStatus.REJECTED;
    }

    // 항목 추가
    public void addItem(ChecklistItem item) {
        this.items.add(item);
        item.setChecklist(this);
    }

    // 사진 추가
    public void addPhoto(Photo photo) {
        this.photos.add(photo);
        photo.setChecklist(this);
    }

    // 위험 항목 개수 조회
    public long getRiskCount() {
        return items.stream()
            .filter(ChecklistItem::getRiskFlag)
            .count();
    }
}

