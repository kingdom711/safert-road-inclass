package com.jinsung.safety_road_inclass.domain.team.repository;

import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findBySiteNameAndNormalizedName(String siteName, String normalizedName);

    boolean existsBySiteNameAndNormalizedName(String siteName, String normalizedName);

    List<Team> findBySiteNameAndNormalizedNameContainingOrderByNameAsc(String siteName, String normalizedNameQuery);

    List<Team> findBySiteNameOrderByNameAsc(String siteName);
}
