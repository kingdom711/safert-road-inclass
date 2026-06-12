package com.jinsung.safety_road_inclass.domain.attendance.repository;

import com.jinsung.safety_road_inclass.domain.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * AttendanceRecordRepository - 출석 기록 데이터 접근
 */
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * 특정 유저의 특정 날짜 출석 여부 확인
     */
    boolean existsByUserIdAndCheckInDate(Long userId, LocalDate checkInDate);

    /**
     * 특정 유저의 기간별 출석 기록 조회 (달력용)
     */
    List<AttendanceRecord> findByUserIdAndCheckInDateBetweenOrderByCheckInDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    long countByUserIdAndCheckInDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT a.user.id, COUNT(a), MAX(a.checkInDate) FROM AttendanceRecord a " +
            "WHERE a.checkInDate BETWEEN :startDate AND :endDate GROUP BY a.user.id")
    List<Object[]> aggregateByUserAndCheckInDateBetween(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}
