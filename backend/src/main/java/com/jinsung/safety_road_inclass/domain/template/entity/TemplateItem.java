package com.jinsung.safety_road_inclass.domain.template.entity;

import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * TemplateItem - 템플릿 항목 Entity
 */
@Entity
@Table(name = "template_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ChecklistTemplate template;

    @Column(nullable = false)
    private Integer itemOrder;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Boolean isRequired = true;

    @Column(length = 100)
    private String category;

    @Builder
    public TemplateItem(Integer itemOrder, String content, Boolean isRequired, String category) {
        this.itemOrder = itemOrder;
        this.content = content;
        this.isRequired = isRequired != null ? isRequired : true;
        this.category = category;
    }

    /**
     * 템플릿 설정 (연관관계 편의 메서드)
     */
    void setTemplate(ChecklistTemplate template) {
        this.template = template;
    }
}

