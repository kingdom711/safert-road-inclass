package com.jinsung.safety_road_inclass.domain.quest.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestDefinition;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuestProgressRepository extends JpaRepository<QuestProgress, Long> {

    Optional<QuestProgress> findByUserAndQuestAndPeriodKey(User user, QuestDefinition quest, String periodKey);

    List<QuestProgress> findAllByUserAndPeriodKey(User user, String periodKey);

    /**
     * 부서 대항전용: 주어진 유저 목록 중 기간 내 퀘스트 완료자(unique) 수
     */
    @Query("SELECT COUNT(DISTINCT qp.user.id) FROM QuestProgress qp " +
            "WHERE qp.user.id IN :userIds AND qp.completed = true " +
            "AND qp.completedAt BETWEEN :start AND :end")
    long countDistinctCompletersByUserIdsAndPeriod(@Param("userIds") Collection<Long> userIds,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(qp) FROM QuestProgress qp " +
            "WHERE qp.user.id = :userId AND qp.completed = true " +
            "AND qp.completedAt >= :start AND qp.completedAt < :end")
    long countCompletedByUserIdAndCompletedAtBetween(@Param("userId") Long userId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    @Query("SELECT qp.user.id, " +
            "SUM(CASE WHEN qp.completed = true AND qp.completedAt >= :start AND qp.completedAt < :end THEN 1 ELSE 0 END), " +
            "COUNT(qp), " +
            "MAX(CASE WHEN qp.completed = true THEN qp.completedAt ELSE qp.updatedAt END) " +
            "FROM QuestProgress qp " +
            "WHERE (qp.completed = true AND qp.completedAt >= :start AND qp.completedAt < :end) " +
            "OR (qp.updatedAt >= :start AND qp.updatedAt < :end) " +
            "GROUP BY qp.user.id")
    List<Object[]> aggregateByUserAndActivityBetween(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);
}
