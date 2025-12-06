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
 * WorkType - 작업 유형 Entity
 * 
 * 예: 사다리 작업, 고소작업대 작업, 밀폐공간 작업 등
 */
@Entity
@Table(name = "work_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkType extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @OneToMany(mappedBy = "workType", cascade = CascadeType.ALL)
    private List<ChecklistTemplate> templates = new ArrayList<>();

    @Builder
    public WorkType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

