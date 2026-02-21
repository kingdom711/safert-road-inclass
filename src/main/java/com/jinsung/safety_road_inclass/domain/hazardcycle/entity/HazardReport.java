package com.jinsung.safety_road_inclass.domain.hazardcycle.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * HazardReport - 위험 발견/조치 완료 1사이클 엔티티
 */
@Entity
@Table(name = "hazard_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HazardReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(name = "hazard_photo_path", nullable = false, length = 500)
    private String hazardPhotoPath;

    @Column(name = "hazard_description", length = 2000)
    private String hazardDescription;

    @Column(name = "location_description", length = 500)
    private String locationDescription;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "ai_risk_level", length = 20)
    private String aiRiskLevel;

    @Column(name = "ai_risk_factor", length = 1000)
    private String aiRiskFactor;

    @Column(name = "ai_remediation_steps", columnDefinition = "TEXT")
    private String aiRemediationSteps;

    @Column(name = "ai_reference_code", length = 50)
    private String aiReferenceCode;

    @Column(name = "ai_analyzed_at")
    private LocalDateTime aiAnalyzedAt;

    @Column(name = "ai_token_usage")
    private Integer aiTokenUsage;

    @Column(name = "completion_photo_path", length = 500)
    private String completionPhotoPath;

    @Column(name = "completion_note", length = 2000)
    private String completionNote;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CycleStatus status;

    @Column(name = "tier1_points_awarded", nullable = false)
    private int tier1PointsAwarded;

    @Column(name = "tier1_awarded_at")
    private LocalDateTime tier1AwardedAt;

    @Column(name = "tier2_points_awarded", nullable = false)
    private int tier2PointsAwarded;

    @Column(name = "tier2_awarded_at")
    private LocalDateTime tier2AwardedAt;

    @Column(name = "client_temp_id", length = 100, unique = true)
    private String clientTempId;

    @Column(name = "synced_from_offline", nullable = false)
    private boolean syncedFromOffline;

    @OneToMany(mappedBy = "hazardReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HazardReportPhoto> photos = new ArrayList<>();

    @Builder
    public HazardReport(User reporter,
                        String hazardPhotoPath,
                        String hazardDescription,
                        String locationDescription,
                        LocalDateTime reportedAt,
                        CycleStatus status,
                        String clientTempId,
                        boolean syncedFromOffline) {
        this.reporter = reporter;
        this.hazardPhotoPath = hazardPhotoPath;
        this.hazardDescription = hazardDescription;
        this.locationDescription = locationDescription;
        this.reportedAt = reportedAt;
        this.status = status != null ? status : CycleStatus.HAZARD_REPORTED;
        this.clientTempId = clientTempId;
        this.syncedFromOffline = syncedFromOffline;
        this.tier1PointsAwarded = 0;
        this.tier2PointsAwarded = 0;
    }

    public void applyAiAnalysis(String riskLevel,
                                String riskFactor,
                                String remediationStepsJson,
                                String referenceCode,
                                Integer tokenUsage,
                                LocalDateTime analyzedAt) {
        this.aiRiskLevel = riskLevel;
        this.aiRiskFactor = riskFactor;
        this.aiRemediationSteps = remediationStepsJson;
        this.aiReferenceCode = referenceCode;
        this.aiTokenUsage = tokenUsage;
        this.aiAnalyzedAt = analyzedAt;
        this.status = CycleStatus.AI_ANALYZED;
    }

    public void completeAction(String completionPhotoPath, String completionNote, LocalDateTime completedAt) {
        this.completionPhotoPath = completionPhotoPath;
        this.completionNote = completionNote;
        this.completedAt = completedAt;
        this.status = CycleStatus.ACTION_COMPLETED;
    }

    public void awardTier1(int points, LocalDateTime awardedAt) {
        this.tier1PointsAwarded = points;
        this.tier1AwardedAt = awardedAt;
    }

    public void awardTier2(int points, LocalDateTime awardedAt) {
        this.tier2PointsAwarded = points;
        this.tier2AwardedAt = awardedAt;
    }

    public int getTotalPointsAwarded() {
        return this.tier1PointsAwarded + this.tier2PointsAwarded;
    }

    public void addPhoto(HazardReportPhoto photo) {
        this.photos.add(photo);
        photo.setHazardReport(this);
    }
}
