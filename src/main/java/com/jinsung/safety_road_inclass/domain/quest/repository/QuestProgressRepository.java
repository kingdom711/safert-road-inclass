package com.jinsung.safety_road_inclass.domain.quest.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestDefinition;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestProgressRepository extends JpaRepository<QuestProgress, Long> {

    Optional<QuestProgress> findByUserAndQuestAndPeriodKey(User user, QuestDefinition quest, String periodKey);

    List<QuestProgress> findAllByUserAndPeriodKey(User user, String periodKey);
}
