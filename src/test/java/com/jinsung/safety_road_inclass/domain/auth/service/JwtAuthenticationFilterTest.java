package com.jinsung.safety_road_inclass.domain.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthenticationFilterTest {

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(mock(JwtTokenProvider.class), new ObjectMapper());

    @Test
    @DisplayName("shouldNotFilter: verify request endpoint remains public")
    void shouldNotFilter_publicVerifyRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/verify/request");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    @DisplayName("shouldNotFilter: verify confirm endpoint still requires auth")
    void shouldNotFilter_verifyConfirmRequiresAuth() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/verify/confirm");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }
}
