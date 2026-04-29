package com.jinsung.safety_road_inclass.domain.team.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "teams",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_teams_site_normalized_name",
                        columnNames = {"site_name", "normalized_name"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 100)
    private String normalizedName;

    @Column(name = "site_name", nullable = false, length = 100)
    private String siteName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leader_user_id", nullable = false)
    private User leader;

    @Builder
    public Team(String name, String normalizedName, String siteName, User leader) {
        this.name = name;
        this.normalizedName = normalizedName;
        this.siteName = siteName;
        this.leader = leader;
    }

    public void rename(String name, String normalizedName) {
        this.name = name;
        this.normalizedName = normalizedName;
    }

    public void changeLeader(User newLeader) {
        this.leader = newLeader;
    }
}
