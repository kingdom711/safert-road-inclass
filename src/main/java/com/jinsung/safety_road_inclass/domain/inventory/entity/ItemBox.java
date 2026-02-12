package com.jinsung.safety_road_inclass.domain.inventory.entity;

import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * ItemBox - 아이템 상자 Entity
 */
@Entity
@Table(name = "item_boxes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemBox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "box_type", nullable = false, length = 20)
    private BoxType boxType;

    @Column(length = 500)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "box", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoxReward> rewards = new ArrayList<>();

    @Builder
    public ItemBox(String name, BoxType boxType, String description, String imageUrl, Boolean active) {
        this.name = name;
        this.boxType = boxType;
        this.description = description;
        this.imageUrl = imageUrl;
        this.active = active != null ? active : true;
    }

    /**
     * 보상 추가
     */
    public void addReward(BoxReward reward) {
        this.rewards.add(reward);
        reward.setBox(this);
    }
}
