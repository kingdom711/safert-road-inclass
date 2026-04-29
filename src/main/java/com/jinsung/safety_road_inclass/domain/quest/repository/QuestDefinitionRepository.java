package com.jinsung.safety_road_inclass.domain.quest.repository;

import com.jinsung.safety_road_inclass.domain.quest.entity.QuestDefinition;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestDefinitionRepository extends JpaRepository<QuestDefinition, Long> {

    Optional<QuestDefinition> findByCode(String code);

    boolean existsByCode(String code);

    List<QuestDefinition> findAllByScopeAndActiveTrueOrderByIdAsc(QuestScope scope);
}
