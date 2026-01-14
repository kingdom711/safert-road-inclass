package com.jinsung.safety_road_inclass.domain.alert.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Alert - 시스템 알림 Entity
 */
@Entity
@Table(name = "alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertType type;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Builder
    public Alert(String title, String message, AlertType type, Integer priority,
                 Boolean active, LocalDateTime startDate, LocalDateTime endDate, User createdBy) {
        this.title = title;
        this.message = message;
        this.type = type != null ? type : AlertType.INFO;
        this.priority = priority != null ? priority : 0;
        this.active = active != null ? active : true;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
    }

    /**
     * 알림 수정
     */
    public void update(String title, String message, AlertType type, Integer priority,
                       Boolean active, LocalDateTime startDate, LocalDateTime endDate) {
        if (title != null) this.title = title;
        if (message != null) this.message = message;
        if (type != null) this.type = type;
        if (priority != null) this.priority = priority;
        if (active != null) this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * 알림 활성화/비활성화
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
