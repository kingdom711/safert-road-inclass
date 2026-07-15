package com.jinsung.safety_road_inclass.domain.education.service;

import com.jinsung.safety_road_inclass.domain.activity.service.ActivityLogService;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.education.dto.EducationCompleteRequest;
import com.jinsung.safety_road_inclass.domain.education.entity.EducationCompletion;
import com.jinsung.safety_road_inclass.domain.education.entity.EducationType;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationCompletionRepository;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationWatchLogRepository;
import com.jinsung.safety_road_inclass.domain.education.repository.QuizAttemptLogRepository;
import com.jinsung.safety_road_inclass.domain.quest.event.EducationCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EducationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EducationCompletionRepository completionRepository;

    @Mock
    private EducationWatchLogRepository watchLogRepository;

    @Mock
    private QuizAttemptLogRepository quizAttemptLogRepository;

    @Mock
    private HashChainService hashChainService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EducationService educationService;

    @BeforeEach
    void setUp() {
        educationService = new EducationService(
                userRepository,
                completionRepository,
                watchLogRepository,
                quizAttemptLogRepository,
                hashChainService,
                activityLogService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("complete returns existing passed completion without publishing a new event")
    void complete_whenAlreadyPassed_returnsExistingCompletion() throws Exception {
        User user = User.builder()
                .username("worker")
                .password("pw")
                .role(Role.ROLE_WORKER)
                .name("Worker")
                .build();

        EducationCompletion existing = EducationCompletion.builder()
                .user(user)
                .educationId("edu_ladder")
                .educationTitle("Ladder Safety")
                .educationType(EducationType.GENERAL)
                .startedAt(LocalDateTime.now().minusMinutes(20))
                .completedAt(LocalDateTime.now().minusMinutes(10))
                .videoWatchSeconds(600)
                .videoTotalSeconds(600)
                .videoWatchRatio(BigDecimal.ONE)
                .quizScore(100)
                .quizPassed(true)
                .attemptCount(1)
                .ipAddress("127.0.0.1")
                .userAgent("test")
                .deviceHash(null)
                .build();
        setField(existing, "id", 44L);
        existing.applyHashChain("CERT-1", "hash", null);

        EducationCompleteRequest request = new EducationCompleteRequest();
        setField(request, "educationId", "edu_ladder");
        setField(request, "educationTitle", "Ladder Safety");
        setField(request, "educationType", EducationType.GENERAL);
        setField(request, "startedAt", LocalDateTime.now().minusMinutes(5).toString());
        setField(request, "videoWatchSeconds", 600);
        setField(request, "videoTotalSeconds", 600);
        setField(request, "videoWatchRatio", BigDecimal.ONE);
        setField(request, "quizScore", 100);
        setField(request, "quizPassed", true);
        setField(request, "attemptCount", 1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(completionRepository.findFirstByUserIdAndEducationIdAndQuizPassedOrderByCompletedAtDesc(
                1L, "edu_ladder", true)).thenReturn(Optional.of(existing));

        var response = educationService.complete(1L, request, "127.0.0.1", "test");

        assertThat(response.getCompletionId()).isEqualTo(44L);
        assertThat(response.getCertNumber()).isEqualTo("CERT-1");
        verify(completionRepository, never()).save(any(EducationCompletion.class));
        verify(eventPublisher, never()).publishEvent(any(EducationCompletedEvent.class));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
