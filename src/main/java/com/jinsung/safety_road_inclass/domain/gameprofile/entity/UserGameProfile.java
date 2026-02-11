package com.jinsung.safety_road_inclass.domain.gameprofile.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UserGameProfile - 유저별 게임 프로필 Entity (1:1)
 * 레벨, 경험치, 게임 역할, 활성 전직 등 게임 진행 정보 저장
 */
@Entity
@Table(name = "user_game_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGameProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private int level = 1;

    @Column(nullable = false)
    private int exp = 0;

    @Column(name = "exp_to_next", nullable = false)
    private int expToNext = 100;

    @Column(name = "game_role", length = 50)
    private String gameRole;

    @Column(name = "active_specialization", length = 50)
    private String activeSpecialization;

    @Column(name = "total_quests_completed", nullable = false)
    private int totalQuestsCompleted = 0;

    public UserGameProfile(User user) {
        this.user = user;
    }

    /**
     * 경험치 추가 및 레벨업 처리
     * 
     * @return 레벨업 횟수
     */
    public int addExp(int amount) {
        this.exp += amount;
        int levelsGained = 0;

        while (this.exp >= this.expToNext) {
            this.exp -= this.expToNext;
            this.level += 1;
            this.expToNext = (int) Math.floor(this.expToNext * 1.5);
            levelsGained++;
        }

        return levelsGained;
    }

    /**
     * 게임 역할 설정
     */
    public void setGameRole(String gameRole) {
        this.gameRole = gameRole;
    }

    /**
     * 활성 전직 설정
     */
    public void setActiveSpecialization(String specId) {
        this.activeSpecialization = specId;
    }

    /**
     * 퀘스트 완료 카운트 증가
     */
    public void incrementQuestsCompleted() {
        this.totalQuestsCompleted++;
    }

    /**
     * localStorage 마이그레이션용 일괄 설정
     */
    public void syncFromData(int level, int exp, int expToNext, String gameRole,
            String activeSpecialization, int totalQuestsCompleted) {
        this.level = level;
        this.exp = exp;
        this.expToNext = expToNext;
        this.gameRole = gameRole;
        this.activeSpecialization = activeSpecialization;
        this.totalQuestsCompleted = totalQuestsCompleted;
    }
}
