package com.jinsung.safety_road_inclass.global.config;

import com.jinsung.safety_road_inclass.domain.quest.entity.QuestConditionType;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestDefinition;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestScope;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestType;
import com.jinsung.safety_road_inclass.domain.quest.repository.QuestDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1100)
public class QuestSeedConfig implements CommandLineRunner {

    private final QuestDefinitionRepository questDefinitionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seed("team_perfect_day", "팀 퍼펙트 데이", "팀원 모두가 일일 개인 퀘스트를 완료합니다.",
                QuestType.DAILY, QuestConditionType.DAILY_ALL_COMPLETE, 1, 300, 100, 0);
        seed("team_hazard_relay", "위험 릴레이", "팀원 모두가 위험요소를 1건 이상 보고합니다.",
                QuestType.DAILY, QuestConditionType.HAZARD_REPORTED, 1, 200, 50, 0);
        seed("team_zero_accident", "무재해 주간", "이번 주 작업중지 신고 없이 주간을 마감합니다.",
                QuestType.WEEKLY, QuestConditionType.ZERO_WORK_STOP, 1, 1000, 300, 500);
        seed("team_education", "전원 안전교육", "팀원 모두가 주간 안전교육을 이수합니다.",
                QuestType.WEEKLY, QuestConditionType.EDUCATION_COMPLETED, 1, 500, 200, 0);
    }

    private void seed(String code, String title, String description, QuestType type,
                      QuestConditionType conditionType, int threshold, int points, int exp, int gold) {
        if (questDefinitionRepository.existsByCode(code)) {
            return;
        }

        questDefinitionRepository.save(QuestDefinition.builder()
                .code(code)
                .title(title)
                .description(description)
                .type(type)
                .scope(QuestScope.TEAM)
                .conditionType(conditionType)
                .threshold(threshold)
                .rewardPoints(points)
                .rewardExp(exp)
                .rewardGold(gold)
                .active(true)
                .build());
        log.info("Quest definition seeded: {}", code);
    }
}
