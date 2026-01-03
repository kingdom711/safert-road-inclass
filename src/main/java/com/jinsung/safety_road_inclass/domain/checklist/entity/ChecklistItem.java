package com.jinsung.safety_road_inclass.domain.checklist.entity;

import com.jinsung.safety_road_inclass.domain.template.entity.TemplateItem;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ChecklistItem - 체크리스트 항목 Entity
 */
@Entity
@Table(name = "checklist_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_item_id", nullable = false)
    private TemplateItem templateItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Answer answer;

    @Column(length = 500)
    private String comment;

    @Column(nullable = false)
    private Boolean riskFlag = false;

    @Builder
    public ChecklistItem(TemplateItem templateItem, Answer answer, String comment) {
        this.templateItem = templateItem;
        this.answer = answer;
        this.comment = comment;
        // "아니오" 응답 시 자동으로 위험 플래그 설정
        this.riskFlag = (answer == Answer.NO);
    }

    void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }
}

