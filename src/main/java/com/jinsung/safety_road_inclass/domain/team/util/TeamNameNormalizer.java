package com.jinsung.safety_road_inclass.domain.team.util;

import java.util.Locale;

/**
 * 팀 이름·현장 이름 정규화 유틸.
 * - 양 끝 공백 제거
 * - 연속 공백 1칸으로 축소
 * - 대소문자 무시 (Locale.ROOT 기준 lower-case)
 *
 * 정규화된 값은 unique 제약과 검색용 LIKE 키로 사용된다.
 */
public final class TeamNameNormalizer {

    private TeamNameNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ");
        return collapsed.toLowerCase(Locale.ROOT);
    }

    public static boolean isBlank(String raw) {
        return raw == null || raw.trim().isEmpty();
    }
}
