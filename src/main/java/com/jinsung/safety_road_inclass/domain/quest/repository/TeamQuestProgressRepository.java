package com.jinsung.safety_road_inclass.domain.quest.repository;

import com.jinsung.safety_road_inclass.domain.quest.entity.QuestDefinition;
import com.jinsung.safety_road_inclass.domain.quest.entity.TeamQuestProgress;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamQuestProgressRepository extends JpaRepository<TeamQuestProgress, Long> {

    Optional<TeamQuestProgress> findByTeamAndQuestAndPeriodKey(Team team, QuestDefinition quest, String periodKey);

    List<TeamQuestProgress> findAllByTeamAndPeriodKey(Team team, String periodKey);
}
