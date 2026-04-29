package com.jinsung.safety_road_inclass.domain.quest.scheduler;

import com.jinsung.safety_road_inclass.domain.quest.service.QuestQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestWeeklyCloseScheduler {

    private final QuestQueryService questQueryService;

    @Scheduled(cron = "0 55 23 * * SUN", zone = "Asia/Seoul")
    public void closeWeeklyZeroWorkStopQuests() {
        LocalDateTime now = LocalDateTime.now();
        questQueryService.closeWeeklyZeroWorkStopQuests(now);
        log.info("Weekly zero-work-stop quests closed: {}", now);
    }
}
