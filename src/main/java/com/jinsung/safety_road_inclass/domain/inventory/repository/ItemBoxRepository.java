package com.jinsung.safety_road_inclass.domain.inventory.repository;

import com.jinsung.safety_road_inclass.domain.inventory.entity.BoxType;
import com.jinsung.safety_road_inclass.domain.inventory.entity.ItemBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ItemBoxRepository - 아이템 상자 데이터 접근
 */
public interface ItemBoxRepository extends JpaRepository<ItemBox, Long> {

    /**
     * 활성화된 상자 조회
     */
    List<ItemBox> findByActiveTrue();

    /**
     * 타입별 상자 조회
     */
    Optional<ItemBox> findByBoxTypeAndActiveTrue(BoxType boxType);

    /**
     * 보상 정보와 함께 조회
     */
    @Query("SELECT DISTINCT b FROM ItemBox b LEFT JOIN FETCH b.rewards WHERE b.id = :id")
    Optional<ItemBox> findByIdWithRewards(@Param("id") Long id);
}
