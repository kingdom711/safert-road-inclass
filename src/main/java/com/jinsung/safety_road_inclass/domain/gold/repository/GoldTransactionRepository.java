package com.jinsung.safety_road_inclass.domain.gold.repository;

import com.jinsung.safety_road_inclass.domain.gold.entity.GoldTransaction;
import com.jinsung.safety_road_inclass.domain.gold.entity.GoldTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GoldTransactionRepository - 골드 거래 내역 저장소
 */
public interface GoldTransactionRepository extends JpaRepository<GoldTransaction, Long> {

    Page<GoldTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT g.user.id, COALESCE(SUM(g.amount), 0), MAX(g.createdAt) FROM GoldTransaction g " +
            "WHERE g.type = :type AND g.createdAt >= :start AND g.createdAt < :end GROUP BY g.user.id")
    List<Object[]> aggregateAmountByUserAndTypeAndCreatedAtBetween(@Param("type") GoldTransactionType type,
                                                                   @Param("start") LocalDateTime start,
                                                                   @Param("end") LocalDateTime end);
}
