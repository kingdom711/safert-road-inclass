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

    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 역할별 사용자 목록 조회
     */
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAllByRole(@Param("role") Role role);

    long countByRoleNotIn(List<Role> roles);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role NOT IN :roles AND LOWER(u.username) <> 'admin'")
    long countParticipants(@Param("roles") List<Role> roles);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.team WHERE u.role NOT IN :roles AND LOWER(u.username) <> 'admin' ORDER BY u.name ASC, u.id ASC")
    List<User> findParticipants(@Param("roles") List<Role> roles);

    @Query(value = """
            SELECT
                u.id,
                u.username,
                u.name,
                u.role,
                u.is_verified,
                u.created_at,
                t.name AS team_name
            FROM users u
            LEFT JOIN teams t ON t.id = u.team_id
            WHERE u.role NOT IN ('ROLE_ADMIN', 'ROLE_PROJECT_ADMIN')
              AND LOWER(u.username) <> 'admin'
            ORDER BY u.name ASC, u.id ASC
            """, nativeQuery = true)
    List<Object[]> findParticipantRows();

    @Query(value = """
            SELECT
                u.id,
                u.username,
                u.name,
                u.email,
                u.role,
                u.team_id,
                t.name AS team_name
            FROM users u
            LEFT JOIN teams t ON t.id = u.team_id
            WHERE u.role NOT IN ('ROLE_ADMIN', 'ROLE_PROJECT_ADMIN')
              AND LOWER(u.username) <> 'admin'
            ORDER BY u.name ASC, u.id ASC
            """, nativeQuery = true)
    List<Object[]> findParticipantAdminRows();

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);
}
