package com.jinsung.safety_road_inclass.domain.quest.event;

import java.time.LocalDateTime;

public record HazardReportedEvent(Long userId, Long reportId, LocalDateTime occurredAt) {
}
