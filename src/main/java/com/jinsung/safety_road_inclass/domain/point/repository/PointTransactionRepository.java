package com.jinsung.safety_road_inclass.domain.point.repository;

import com.jinsung.safety_road_inclass.domain.point.entity.PointTransaction;
import com.jinsung.safety_road_inclass.domain.point.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PointTransactionRepository - 포인트 거래 내역 데이터 접근
 */
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    /**
     * 유저별 거래 내역 페이징 조회 (최신순)
     */
    Page<PointTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PointTransaction p " +
            "WHERE p.user.id = :userId AND p.type = :type " +
            "AND p.createdAt >= :start AND p.createdAt < :end")
    int sumAmountByUserIdAndTypeAndCreatedAtBetween(@Param("userId") Long userId,
                                                    @Param("type") TransactionType type,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    @Query("SELECT p.user.id, COALESCE(SUM(p.amount), 0), MAX(p.createdAt) FROM PointTransaction p " +
            "WHERE p.type = :type AND p.createdAt >= :start AND p.createdAt < :end GROUP BY p.user.id")
    List<Object[]> aggregateAmountByUserAndTypeAndCreatedAtBetween(@Param("type") TransactionType type,
                                                                   @Param("start") LocalDateTime start,
                                                                   @Param("end") LocalDateTime end);
}
