package com.jinsung.safety_road_inclass.domain.quest.util;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

public final class PeriodKeyResolver {

    private static final WeekFields ISO_WEEK = WeekFields.ISO;

    private PeriodKeyResolver() {
    }

    public static String dailyKey(LocalDateTime kst) {
        return "%04d%02d%02d".formatted(kst.getYear(), kst.getMonthValue(), kst.getDayOfMonth());
    }

    public static String weeklyKey(LocalDateTime kst) {
        int weekBasedYear = kst.get(ISO_WEEK.weekBasedYear());
        int weekOfYear = kst.get(ISO_WEEK.weekOfWeekBasedYear());
        return String.format(Locale.ROOT, "%04d-W%02d", weekBasedYear, weekOfYear);
    }
}
