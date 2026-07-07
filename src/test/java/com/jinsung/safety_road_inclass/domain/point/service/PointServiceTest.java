package com.jinsung.safety_road_inclass.domain.point.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.point.entity.PointTransaction;
import com.jinsung.safety_road_inclass.domain.point.entity.TransactionType;
import com.jinsung.safety_road_inclass.domain.point.entity.UserPoints;
import com.jinsung.safety_road_inclass.domain.point.repository.PointTransactionRepository;
import com.jinsung.safety_road_inclass.domain.point.repository.UserPointsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointsRepository userPointsRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private UserRepository userRepository;

    private PointService pointService;

    @BeforeEach
    void setUp() {
        pointService = new PointService(userPointsRepository, pointTransactionRepository, userRepository);
    }

    @Test
    @DisplayName("addPoints ignores duplicate education completion rewards")
    void addPoints_ignoresDuplicateEducationReward() {
        User user = User.builder()
                .username("worker")
                .password("pw")
                .role(Role.ROLE_WORKER)
                .name("Worker")
                .build();
        UserPoints userPoints = new UserPoints(user);
        userPoints.earn(50);

        String reason = "\uAD50\uC721 \uC644\uB8CC";
        String description = "Ladder Safety \uC644\uB8CC";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(pointTransactionRepository.existsByUserIdAndTypeAndReasonAndDescription(
                1L, TransactionType.EARN, reason, description)).thenReturn(true);
        when(userPointsRepository.findByUserId(1L)).thenReturn(Optional.of(userPoints));

        var response = pointService.addPoints(1L, 50, reason, description);

        assertThat(response.getAmount()).isZero();
        assertThat(response.getBalanceAfter()).isEqualTo(50);
        verify(pointTransactionRepository, never()).save(any(PointTransaction.class));
    }
}
