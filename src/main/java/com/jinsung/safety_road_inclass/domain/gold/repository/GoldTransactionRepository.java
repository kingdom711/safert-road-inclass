package com.jinsung.safety_road_inclass.domain.gold.repository;

import com.jinsung.safety_road_inclass.domain.gold.entity.GoldTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * GoldTransactionRepository - 골드 거래 내역 저장소
 */
public interface GoldTransactionRepository extends JpaRepository<GoldTransaction, Long> {

    Page<GoldTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
