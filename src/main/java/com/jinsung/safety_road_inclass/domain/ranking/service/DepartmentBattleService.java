package com.jinsung.safety_road_inclass.domain.ranking.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.checklist.repository.ChecklistRepository;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationCompletionRepository;
import com.jinsung.safety_road_inclass.domain.quest.repository.QuestProgressRepository;
import com.jinsung.safety_road_inclass.domain.ranking.dto.DepartmentBattleResponse;
import com.jinsung.safety_road_inclass.domain.ranking.dto.DepartmentBattleResponse.CategoryMeta;
import com.jinsung.safety_road_inclass.domain.ranking.dto.DepartmentBattleResponse.DepartmentEntry;
import com.jinsung.safety_road_inclass.domain.ranking.dto.DepartmentBattleResponse.RewardEntry;
import com.jinsung.safety_road_inclass.domain.risk.repository.RiskAssessmentRepository;
import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamMemberRepository;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DepartmentBattleService - 부서 대항전 점수/순위 집계
 *
 * 카테고리 (총 100점):
 *  - questParticipation (30): 이번 달 ≥1회 퀘스트 완료한 멤버 비율 × 30
 *  - educationRate     (25): 이번 달 교육 합격(quizPassed) 고유 멤버 비율 × 25
 *  - hazardReports     (20): 이번 달 위험성 평가 등록 건수, 1인 평균 2건 기준 캡 후 × 20
 *  - safeDays          (15): 이번 달 HIGH/CRITICAL 위험성 0건이면 만점, 건당 -3점 (하한 0)
 *  - checklistRate     (10): 이번 달 체크리스트(SUBMITTED 이상) 제출한 고유 멤버 비율 × 10
 *
 * 점수는 소수점 1자리, totalScore는 합산 후 1자리 반올림.
 * rankChange는 과거 스냅샷이 없으므로 현재 0 고정.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DepartmentBattleService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final double WEIGHT_QUEST = 30.0;
    private static final double WEIGHT_EDUCATION = 25.0;
    private static final double WEIGHT_HAZARD = 20.0;
    private static final double WEIGHT_SAFE_DAYS = 15.0;
    private static final double WEIGHT_CHECKLIST = 10.0;

    /** 인당 위험성 평가 만점 기준 건수 (이 값에 도달하면 hazardReports 만점) */
    private static final double HAZARD_PER_MEMBER_TARGET = 2.0;
    /** safeDays 점수에서 고위험 1건당 차감 점수 */
    private static final double SAFE_DAYS_PENALTY_PER_HIGH = 3.0;

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final QuestProgressRepository questProgressRepository;
    private final EducationCompletionRepository educationCompletionRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ChecklistRepository checklistRepository;

    public DepartmentBattleResponse getBattle(Long currentUserId) {
        LocalDate today = LocalDate.now(KST);
        YearMonth month = YearMonth.from(today);
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);
        int remainingDays = Math.max(0, month.lengthOfMonth() - today.getDayOfMonth());

        List<DepartmentEntry> entries = new ArrayList<>();

        for (Team team : teamRepository.findAll()) {
            List<TeamMember> activeMembers = teamMemberRepository.findAllByTeamAndStatus(team, MembershipStatus.ACTIVE);
            int memberCount = activeMembers.size();
            if (memberCount == 0) {
                continue;
            }

            Set<Long> userIds = activeMembers.stream()
                    .map(TeamMember::getUser)
                    .map(User::getId)
                    .collect(Collectors.toSet());

            long questCompleters = questProgressRepository
                    .countDistinctCompletersByUserIdsAndPeriod(userIds, start, end);
            long educationPassers = educationCompletionRepository
                    .countDistinctPassedUsersByUserIdsAndPeriod(userIds, start, end);
            long hazardCount = riskAssessmentRepository
                    .countByAssessorUserIdsAndPeriod(userIds, start, end);
            long highRiskCount = riskAssessmentRepository
                    .countHighRiskByAssessorUserIdsAndPeriod(userIds, start, end);
            long checklistSubmitters = checklistRepository
                    .countDistinctSubmittersByUserIdsAndPeriod(userIds, start, end);

            double questScore = ratio(questCompleters, memberCount) * WEIGHT_QUEST;
            double educationScore = ratio(educationPassers, memberCount) * WEIGHT_EDUCATION;
            double hazardScore = Math.min(1.0,
                    (double) hazardCount / (HAZARD_PER_MEMBER_TARGET * memberCount)) * WEIGHT_HAZARD;
            double safeDaysScore = Math.max(0.0,
                    WEIGHT_SAFE_DAYS - highRiskCount * SAFE_DAYS_PENALTY_PER_HIGH);
            double checklistScore = ratio(checklistSubmitters, memberCount) * WEIGHT_CHECKLIST;

            Map<String, Double> scores = new LinkedHashMap<>();
            scores.put("questParticipation", round1(questScore));
            scores.put("educationRate", round1(educationScore));
            scores.put("hazardReports", round1(hazardScore));
            scores.put("safeDays", round1(safeDaysScore));
            scores.put("checklistRate", round1(checklistScore));

            double total = scores.values().stream().mapToDouble(Double::doubleValue).sum();

            entries.add(DepartmentEntry.builder()
                    .id(team.getId())
                    .name(team.getName())
                    .memberCount(memberCount)
                    .totalScore(round1(total))
                    .scores(scores)
                    .rankChange(0)
                    .build());
        }

        entries.sort(Comparator.comparingDouble(DepartmentEntry::getTotalScore).reversed());
        List<DepartmentEntry> ranked = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            DepartmentEntry e = entries.get(i);
            ranked.add(DepartmentEntry.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .memberCount(e.getMemberCount())
                    .rank(i + 1)
                    .totalScore(e.getTotalScore())
                    .scores(e.getScores())
                    .rankChange(e.getRankChange())
                    .build());
        }

        Long myDeptId = resolveMyDeptId(currentUserId);

        return DepartmentBattleResponse.builder()
                .month(month.toString())
                .remainingDays(remainingDays)
                .myDeptId(myDeptId)
                .departments(ranked)
                .categories(buildCategoryMeta())
                .rewards(buildRewards())
                .build();
    }

    private Long resolveMyDeptId(Long userId) {
        if (userId == null) return null;
        return teamMemberRepository.findAll().stream()
                .filter(tm -> tm.getStatus() == MembershipStatus.ACTIVE
                        && tm.getUser() != null
                        && userId.equals(tm.getUser().getId()))
                .findFirst()
                .map(tm -> tm.getTeam().getId())
                .orElse(null);
    }

    private static double ratio(long numerator, int denominator) {
        if (denominator <= 0) return 0.0;
        return Math.min(1.0, (double) numerator / denominator);
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static Map<String, CategoryMeta> buildCategoryMeta() {
        Map<String, CategoryMeta> map = new LinkedHashMap<>();
        map.put("questParticipation", CategoryMeta.builder().label("퀘스트 참여율").maxScore(WEIGHT_QUEST).build());
        map.put("educationRate", CategoryMeta.builder().label("교육 이수율").maxScore(WEIGHT_EDUCATION).build());
        map.put("hazardReports", CategoryMeta.builder().label("위험 신고 건수").maxScore(WEIGHT_HAZARD).build());
        map.put("safeDays", CategoryMeta.builder().label("무재해 일수").maxScore(WEIGHT_SAFE_DAYS).build());
        map.put("checklistRate", CategoryMeta.builder().label("체크리스트 제출률").maxScore(WEIGHT_CHECKLIST).build());
        return map;
    }

    private static List<RewardEntry> buildRewards() {
        // 1위 5000P 기준, 2위부터 직전 순위의 50% 체감
        List<RewardEntry> list = new ArrayList<>();
        list.add(RewardEntry.builder().rank(1).reward("팀원 전원 5,000P").points(5000).icon("🥇").build());
        list.add(RewardEntry.builder().rank(2).reward("팀원 전원 2,500P").points(2500).icon("🥈").build());
        list.add(RewardEntry.builder().rank(3).reward("팀원 전원 1,250P").points(1250).icon("🥉").build());
        list.add(RewardEntry.builder().rank(0).reward("참여 부서 전원 625P").points(625).icon("🏅").build());
        return list;
    }
}
