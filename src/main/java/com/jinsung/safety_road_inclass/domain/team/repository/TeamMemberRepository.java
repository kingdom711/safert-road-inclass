package com.jinsung.safety_road_inclass.domain.team.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeamAndUser(Team team, User user);

    Optional<TeamMember> findByUserAndStatus(User user, MembershipStatus status);

    boolean existsByUserAndStatus(User user, MembershipStatus status);

    List<TeamMember> findAllByTeamAndStatus(Team team, MembershipStatus status);

    List<TeamMember> findAllByTeamAndStatusAndUserRoleNotIn(Team team, MembershipStatus status, List<Role> roles);

    List<TeamMember> findAllByUser(User user);

    long countByTeamAndStatus(Team team, MembershipStatus status);

    long countByTeamAndStatusAndUserRoleNotIn(Team team, MembershipStatus status, List<Role> roles);
}
