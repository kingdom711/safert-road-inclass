package com.jinsung.safety_road_inclass.domain.team.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamNameNormalizerTest {

    @Test
    @DisplayName("앞뒤 공백 제거 + 연속 공백 축소 + lower-case")
    void normalizesWhitespaceAndCase() {
        assertThat(TeamNameNormalizer.normalize("  철근팀  A조  ")).isEqualTo("철근팀 a조");
        assertThat(TeamNameNormalizer.normalize("Rebar  TEAM")).isEqualTo("rebar team");
    }

    @Test
    @DisplayName("null 입력 시 빈 문자열 반환")
    void nullReturnsEmpty() {
        assertThat(TeamNameNormalizer.normalize(null)).isEmpty();
    }

    @Test
    @DisplayName("isBlank 검사")
    void isBlankDetectsEmpty() {
        assertThat(TeamNameNormalizer.isBlank(null)).isTrue();
        assertThat(TeamNameNormalizer.isBlank("")).isTrue();
        assertThat(TeamNameNormalizer.isBlank("   ")).isTrue();
        assertThat(TeamNameNormalizer.isBlank("  x  ")).isFalse();
    }
}
