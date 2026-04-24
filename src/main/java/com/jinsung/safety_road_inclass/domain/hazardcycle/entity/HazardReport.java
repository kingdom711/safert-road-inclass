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

    // 확장 위험성평가 필드 (고용노동부 고시 제2023-19호)
    @Column(name = "ai_hazard_classification", length = 50)
    private String aiHazardClassification;

    @Column(name = "ai_unsafe_condition", length = 1000)
    private String aiUnsafeCondition;

    @Column(name = "ai_unsafe_act", length = 1000)
    private String aiUnsafeAct;

    @Column(name = "ai_possible_accident", length = 100)
    private String aiPossibleAccident;

    @Column(name = "ai_severity_score")
    private Integer aiSeverityScore;

    @Column(name = "ai_severity_rationale", length = 500)
    private String aiSeverityRationale;

    @Column(name = "ai_likelihood_score")
    private Integer aiLikelihoodScore;

    @Column(name = "ai_likelihood_rationale", length = 500)
    private String aiLikelihoodRationale;

    @Column(name = "ai_risk_score")
    private Integer aiRiskScore;

    @Column(name = "ai_legal_basis_json", columnDefinition = "TEXT")
    private String aiLegalBasisJson;

    @Column(name = "ai_kosha_guide", length = 50)
    private String aiKoshaGuide;

    @Column(name = "ai_control_measures_json", columnDefinition = "TEXT")
    private String aiControlMeasuresJson;

    @Column(name = "ai_responsible_role", length = 50)
    private String aiResponsibleRole;

    @Column(name = "ai_due_days")
    private Integer aiDueDays;

    @Column(name = "ai_confidence")
    private Double aiConfidence;

    // 사진 해시(SHA-256 hex) — 동일 사진 재분석 방지용 캐시 키
    @Column(name = "photo_sha256", length = 64)
    private String photoSha256;

    // 캐시 재사용 시 원본 사이클 ID (감사 추적용)
    @Column(name = "ai_cache_source_id")
    private Long aiCacheSourceId;

    // 무결성 해시체인 — 생성 시 즉시 확정, 이후 수정 금지(데이터 조작 증명)
    @Column(name = "cert_number", length = 36, unique = true)
    private String certNumber;

    @Column(name = "prev_hash", length = 64)
    private String prevHash;

    @Column(name = "integrity_hash", length = 64)
    private String integrityHash;

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

    /**
     * 확장 위험성평가 결과 적용.
     * applyAiAnalysis()로 기본 필드 설정 후 호출한다.
     */
    public void applyExtendedAnalysis(String hazardClassification,
                                      String unsafeCondition,
                                      String unsafeAct,
                                      String possibleAccident,
                                      Integer severityScore,
                                      String severityRationale,
                                      Integer likelihoodScore,
                                      String likelihoodRationale,
                                      Integer riskScore,
                                      String legalBasisJson,
                                      String koshaGuide,
                                      String controlMeasuresJson,
                                      String responsibleRole,
                                      Integer dueDays,
                                      Double confidence) {
        this.aiHazardClassification = hazardClassification;
        this.aiUnsafeCondition = unsafeCondition;
        this.aiUnsafeAct = unsafeAct;
        this.aiPossibleAccident = possibleAccident;
        this.aiSeverityScore = severityScore;
        this.aiSeverityRationale = severityRationale;
        this.aiLikelihoodScore = likelihoodScore;
        this.aiLikelihoodRationale = likelihoodRationale;
        this.aiRiskScore = riskScore;
        this.aiLegalBasisJson = legalBasisJson;
        this.aiKoshaGuide = koshaGuide;
        this.aiControlMeasuresJson = controlMeasuresJson;
        this.aiResponsibleRole = responsibleRole;
        this.aiDueDays = dueDays;
        this.aiConfidence = confidence;
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

    public void setPhotoSha256(String sha256) {
        this.photoSha256 = sha256;
    }

    public void markCacheReuse(Long sourceId) {
        this.aiCacheSourceId = sourceId;
    }

    public void sealIntegrity(String certNumber, String prevHash, String integrityHash) {
        if (this.integrityHash != null) {
            // 이미 확정된 해시체인은 재계산 금지
            return;
        }
        this.certNumber = certNumber;
        this.prevHash = prevHash;
        this.integrityHash = integrityHash;
    }

    /**
     * 다른 HazardReport의 AI 분석 결과를 이 사이클에 복사.
     * 캐시 히트 시 호출한다.
     */
    public void copyAiAnalysisFrom(HazardReport source, LocalDateTime analyzedAt) {
        this.aiRiskLevel = source.aiRiskLevel;
        this.aiRiskFactor = source.aiRiskFactor;
        this.aiRemediationSteps = source.aiRemediationSteps;
        this.aiReferenceCode = source.aiReferenceCode;
        this.aiTokenUsage = 0;
        this.aiAnalyzedAt = analyzedAt;
        this.aiHazardClassification = source.aiHazardClassification;
        this.aiUnsafeCondition = source.aiUnsafeCondition;
        this.aiUnsafeAct = source.aiUnsafeAct;
        this.aiPossibleAccident = source.aiPossibleAccident;
        this.aiSeverityScore = source.aiSeverityScore;
        this.aiSeverityRationale = source.aiSeverityRationale;
        this.aiLikelihoodScore = source.aiLikelihoodScore;
        this.aiLikelihoodRationale = source.aiLikelihoodRationale;
        this.aiRiskScore = source.aiRiskScore;
        this.aiLegalBasisJson = source.aiLegalBasisJson;
        this.aiKoshaGuide = source.aiKoshaGuide;
        this.aiControlMeasuresJson = source.aiControlMeasuresJson;
        this.aiResponsibleRole = source.aiResponsibleRole;
        this.aiDueDays = source.aiDueDays;
        this.aiConfidence = source.aiConfidence;
        this.aiCacheSourceId = source.id;
        this.status = CycleStatus.AI_ANALYZED;
    }
}
