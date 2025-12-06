package com.jinsung.safety_road_inclass.domain.template.entity;

import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * ChecklistTemplate - 체크리스트 템플릿 Entity
 */
@Entity
@Table(name = "checklist_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_type_id", nullable = false)
    private WorkType workType;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Integer version = 1;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemOrder ASC")
    private List<TemplateItem> items = new ArrayList<>();

    @Builder
    public ChecklistTemplate(WorkType workType, String title, String description) {
        this.workType = workType;
        this.title = title;
        this.description = description;
    }

    /**
     * 항목 추가
     */
    public void addItem(TemplateItem item) {
        this.items.add(item);
        item.setTemplate(this);
    }

    /**
     * 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 버전 증가
     */
    public void incrementVersion() {
        this.version++;
    }
}

