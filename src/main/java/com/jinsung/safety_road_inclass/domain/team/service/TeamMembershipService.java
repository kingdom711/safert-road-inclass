package com.jinsung.safety_road_inclass.domain.team.service;

import com.jinsung.safety_road_inclass.domain.alert.entity.AlertType;
import com.jinsung.safety_road_inclass.domain.alert.service.AlertService;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.team.dto.MembershipResponse;
import com.jinsung.safety_road_inclass.domain.team.dto.PendingMemberResponse;
import com.jinsung.safety_road_inclass.domain.team.dto.TeamMemberResponse;
import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamMemberRepository;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TeamMembershipService - 가입 / 승인 / 거절 / 탈퇴 / 강퇴 (B3)
 *
 * 정책:
 *  - 한 사용자당 ACTIVE 멤버십 1개 (생성 시 차단)
 *  - 승인은 리더만 가능
 *  - 본인이 본인을 강퇴/거절하는 케이스는 차단 (탈퇴 API 사용)
 *  - LEFT 상태인 동일 (team,user) 페어가 있으면 PENDING으로 재활성
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TeamMembershipService {

    private static final List<Role> NON_PARTICIPANT_ROLES =
            List.of(Role.ROLE_ADMIN, Role.ROLE_PROJECT_ADMIN);

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final AlertService alertService;

    public MembershipResponse getCurrentMembership(Long userId) {
        User user = getUser(userId);
        if (!isParticipant(user)) {
            return null;
        }
        return teamMemberRepository.findByUserAndStatus(user, MembershipStatus.ACTIVE)
                .or(() -> teamMemberRepository.findByUserAndStatus(user, MembershipStatus.PENDING))
                .or(() -> teamMemberRepository.findByUserAndStatus(user, MembershipStatus.LEFT))
                .map(MembershipResponse::of)
                .orElse(null);
    }

    @Transactional
    public MembershipResponse requestJoin(Long teamId, Long applicantUserId) {
        Team team = getTeam(teamId);
        User applicant = getUser(applicantUserId);
        ensureParticipant(applicant);

        if (teamMemberRepository.existsByUserAndStatus(applicant, MembershipStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.TEAM_ALREADY_JOINED);
        }

        TeamMember membership = teamMemberRepository.findByTeamAndUser(team, applicant)
                .map(existing -> reactivateIfLeftOrFail(existing))
                .orElseGet(() -> teamMemberRepository.save(
                        TeamMember.builder()
                                .team(team)
                                .user(applicant)
                                .status(MembershipStatus.PENDING)
                                .joinedAt(LocalDateTime.now())
                                .build()));

        notifyTeamSafely(
                team.getLeader(),
                applicant,
                AlertType.TEAM_JOIN_REQUESTED,
                "신규 팀 가입 신청",
                applicant.getName() + "님이 " + team.getName() + " 가입을 신청했습니다.");

        log.info("Team join requested: team={}, user={}", teamId, applicantUserId);
        return MembershipResponse.of(membership);
    }

    private TeamMember reactivateIfLeftOrFail(TeamMember existing) {
        switch (existing.getStatus()) {
            case PENDING -> throw new CustomException(ErrorCode.TEAM_ALREADY_JOINED, "이미 신청 대기 중입니다.");
            case ACTIVE -> throw new CustomException(ErrorCode.TEAM_ALREADY_JOINED);
            case LEFT -> existing.requestRejoin();
        }
        return existing;
    }

    public List<PendingMemberResponse> listPending(Long teamId, Long requesterUserId) {
        Team team = getTeam(teamId);
        ensureLeader(team, requesterUserId);
        return teamMemberRepository.findAllByTeamAndStatus(team, MembershipStatus.PENDING).stream()
                .map(PendingMemberResponse::of)
                .toList();
    }

    public List<TeamMemberResponse> listActiveMembers(Long teamId, Long requesterUserId) {
        Team team = getTeam(teamId);
        ensureActiveMember(team, requesterUserId);
        return teamMemberRepository.findAllByTeamAndStatusAndUserRoleNotIn(team, MembershipStatus.ACTIVE, NON_PARTICIPANT_ROLES).stream()
                .map(TeamMemberResponse::of)
                .toList();
    }

    @Transactional
    public MembershipResponse approve(Long teamId, Long targetUserId, Long requesterUserId) {
        Team team = getTeam(teamId);
        ensureLeader(team, requesterUserId);
        User target = getUser(targetUserId);
        User requester = getUser(requesterUserId);

        TeamMember member = teamMemberRepository.findByTeamAndUser(team, target)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBERSHIP_NOT_FOUND));

        if (!member.isPending()) {
            throw new CustomException(ErrorCode.TEAM_MEMBERSHIP_INVALID_STATUS);
        }

        if (teamMemberRepository.existsByUserAndStatus(target, MembershipStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.TEAM_ALREADY_JOINED, "신청자가 이미 다른 팀의 ACTIVE 멤버입니다.");
        }

        member.approve(requester);
        target.assignTeam(team);

        notifyTeamSafely(
                target,
                requester,
                AlertType.TEAM_JOIN_APPROVED,
                "팀 가입 승인",
                team.getName() + " 가입 신청이 승인되었습니다.");

        log.info("Team join approved: team={}, target={}, by={}", teamId, targetUserId, requesterUserId);
        return MembershipResponse.of(member);
    }

    @Transactional
    public void reject(Long teamId, Long targetUserId, Long requesterUserId) {
        Team team = getTeam(teamId);
        ensureLeader(team, requesterUserId);
        User target = getUser(targetUserId);

        TeamMember member = teamMemberRepository.findByTeamAndUser(team, target)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBERSHIP_NOT_FOUND));

        if (!member.isPending()) {
            throw new CustomException(ErrorCode.TEAM_MEMBERSHIP_INVALID_STATUS);
        }

        teamMemberRepository.delete(member);
        notifyTeamSafely(
                target,
                team.getLeader(),
                AlertType.TEAM_JOIN_REJECTED,
                "팀 가입 거절",
                team.getName() + " 가입 신청이 거절되었습니다.");
        log.info("Team join rejected: team={}, target={}, by={}", teamId, targetUserId, requesterUserId);
    }

    @Transactional
    public void leave(Long teamId, Long requesterUserId) {
        Team team = getTeam(teamId);
        User requester = getUser(requesterUserId);

        TeamMember member = teamMemberRepository.findByTeamAndUser(team, requester)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBERSHIP_NOT_FOUND));

        if (!member.isActive()) {
            throw new CustomException(ErrorCode.TEAM_MEMBERSHIP_INVALID_STATUS);
        }

        if (team.getLeader().getId().equals(requesterUserId)) {
            throw new CustomException(ErrorCode.TEAM_LEADER_FORBIDDEN, "리더는 탈퇴 전에 리더를 위임해야 합니다.");
        }

        member.leave();
        if (requester.getTeam() != null && requester.getTeam().getId().equals(teamId)) {
            requester.clearTeam();
        }
        log.info("Team left: team={}, user={}", teamId, requesterUserId);
    }

    @Transactional
    public void kick(Long teamId, Long targetUserId, Long requesterUserId) {
        Team team = getTeam(teamId);
        ensureLeader(team, requesterUserId);

        if (targetUserId.equals(requesterUserId)) {
            throw new CustomException(ErrorCode.TEAM_LEADER_FORBIDDEN, "본인을 강퇴할 수 없습니다.");
        }
        if (team.getLeader().getId().equals(targetUserId)) {
            throw new CustomException(ErrorCode.TEAM_LEADER_FORBIDDEN, "리더는 강퇴할 수 없습니다.");
        }

        User target = getUser(targetUserId);
        TeamMember member = teamMemberRepository.findByTeamAndUser(team, target)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBERSHIP_NOT_FOUND));

        if (!member.isActive()) {
            throw new CustomException(ErrorCode.TEAM_MEMBERSHIP_INVALID_STATUS);
        }

        member.leave();
        if (target.getTeam() != null && target.getTeam().getId().equals(teamId)) {
            target.clearTeam();
        }
        notifyTeamSafely(
                target,
                team.getLeader(),
                AlertType.TEAM_MEMBER_KICKED,
                "팀에서 제외됨",
                team.getName() + "에서 제외되었습니다.");
        log.info("Team kicked: team={}, target={}, by={}", teamId, targetUserId, requesterUserId);
    }

    private Team getTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void ensureLeader(Team team, Long requesterUserId) {
        if (!team.getLeader().getId().equals(requesterUserId)) {
            throw new CustomException(ErrorCode.TEAM_LEADER_FORBIDDEN);
        }
    }

    private void ensureActiveMember(Team team, Long requesterUserId) {
        User requester = getUser(requesterUserId);
        ensureParticipant(requester);
        TeamMember membership = teamMemberRepository.findByTeamAndUser(team, requester)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_MEMBERSHIP_NOT_FOUND));
        if (!membership.isActive()) {
            throw new CustomException(ErrorCode.TEAM_MEMBERSHIP_INVALID_STATUS);
        }
    }

    private void ensureParticipant(User user) {
        if (!isParticipant(user)) {
            throw new CustomException(ErrorCode.TEAM_MEMBERSHIP_NOT_FOUND);
        }
    }

    private boolean isParticipant(User user) {
        return user != null
                && !NON_PARTICIPANT_ROLES.contains(user.getRole())
                && !"admin".equalsIgnoreCase(user.getUsername());
    }

    private void notifyTeamSafely(User recipient, User actor, AlertType type, String title, String message) {
        try {
            alertService.createTeamNotification(recipient, actor, type, title, message);
        } catch (Exception e) {
            log.warn("Team notification failed but membership action will continue: type={}, recipient={}, actor={}",
                    type,
                    recipient != null ? recipient.getId() : null,
                    actor != null ? actor.getId() : null,
                    e);
        }
    }
}
