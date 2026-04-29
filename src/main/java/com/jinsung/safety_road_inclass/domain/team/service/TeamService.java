package com.jinsung.safety_road_inclass.domain.team.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.team.dto.TeamCreateRequest;
import com.jinsung.safety_road_inclass.domain.team.dto.TeamSummaryResponse;
import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamMemberRepository;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamRepository;
import com.jinsung.safety_road_inclass.domain.team.util.TeamNameNormalizer;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TeamService - 팀 검색·생성 (B2)
 *
 * - search: 현장 + 부분 일치 자동완성 (정규화 LIKE, 최대 20건)
 * - create: 동일 현장 내 정규화 이름 중복 차단, 생성자가 ACTIVE 리더로 자동 등록
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeamService {

    private static final int SEARCH_LIMIT = 20;

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public List<TeamSummaryResponse> search(String siteName, String query) {
        if (TeamNameNormalizer.isBlank(siteName)) {
            throw new CustomException(ErrorCode.MISSING_PARAMETER, "siteName은 필수입니다.");
        }
        String normalizedSite = TeamNameNormalizer.normalize(siteName);

        List<Team> teams;
        if (TeamNameNormalizer.isBlank(query)) {
            teams = teamRepository.findBySiteNameOrderByNameAsc(normalizedSite);
        } else {
            String normalizedQuery = TeamNameNormalizer.normalize(query);
            teams = teamRepository.findBySiteNameAndNormalizedNameContainingOrderByNameAsc(
                    normalizedSite, normalizedQuery);
        }

        return teams.stream()
                .limit(SEARCH_LIMIT)
                .map(team -> TeamSummaryResponse.of(
                        team,
                        teamMemberRepository.countByTeamAndStatus(team, MembershipStatus.ACTIVE)))
                .toList();
    }

    @Transactional
    public TeamSummaryResponse create(Long creatorUserId, TeamCreateRequest request) {
        if (TeamNameNormalizer.isBlank(request.getName()) || TeamNameNormalizer.isBlank(request.getSiteName())) {
            throw new CustomException(ErrorCode.TEAM_INVALID_NAME);
        }

        String normalizedSite = TeamNameNormalizer.normalize(request.getSiteName());
        String normalizedName = TeamNameNormalizer.normalize(request.getName());

        if (teamRepository.existsBySiteNameAndNormalizedName(normalizedSite, normalizedName)) {
            throw new CustomException(ErrorCode.TEAM_NAME_DUPLICATED);
        }

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (teamMemberRepository.existsByUserAndStatus(creator, MembershipStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.TEAM_ALREADY_JOINED);
        }

        try {
            Team team = Team.builder()
                    .name(request.getName().trim())
                    .normalizedName(normalizedName)
                    .siteName(normalizedSite)
                    .leader(creator)
                    .build();
            teamRepository.saveAndFlush(team);

            TeamMember leaderMembership = TeamMember.builder()
                    .team(team)
                    .user(creator)
                    .status(MembershipStatus.PENDING)
                    .joinedAt(LocalDateTime.now())
                    .build();
            leaderMembership.approve(creator);
            teamMemberRepository.saveAndFlush(leaderMembership);

            creator.assignTeam(team);
            userRepository.saveAndFlush(creator);

            log.info("Team created: id={}, name={}, site={}, leader={}",
                    team.getId(), team.getName(), team.getSiteName(), creator.getId());

            return TeamSummaryResponse.of(team, 1L);
        } catch (CustomException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.warn("Team create conflict: site={}, name={}, creator={}", normalizedSite, normalizedName, creatorUserId, e);
            throw new CustomException(ErrorCode.TEAM_NAME_DUPLICATED);
        } catch (Exception e) {
            log.error("Team create failed: site={}, name={}, creator={}, cause={}: {}",
                    normalizedSite, normalizedName, creatorUserId,
                    e.getClass().getName(), e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "팀 생성 중 오류: " + e.getClass().getSimpleName()
                            + (e.getMessage() != null ? " - " + e.getMessage() : ""));
        }
    }
}
