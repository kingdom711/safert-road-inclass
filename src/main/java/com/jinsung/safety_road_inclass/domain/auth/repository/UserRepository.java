package com.jinsung.safety_road_inclass.domain.auth.repository;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository - 사용자 데이터 접근
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * username으로 사용자 조회
     */
    Optional<User> findByUsername(String username);

    /**
     * username 중복 체크
     */
    boolean existsByUsername(String username);

    /**
     * 역할별 사용자 목록 조회
     */
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAllByRole(@Param("role") Role role);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);
}

