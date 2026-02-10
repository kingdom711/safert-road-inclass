package com.jinsung.safety_road_inclass.domain.point.repository;

import com.jinsung.safety_road_inclass.domain.point.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PointTransactionRepository - 포인트 거래 내역 데이터 접근
 */
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    /**
     * 유저별 거래 내역 페이징 조회 (최신순)
     */
    Page<PointTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
