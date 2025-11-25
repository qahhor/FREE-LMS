package com.freelms.gamification.repository;

import com.freelms.gamification.entity.Achievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    Page<Achievement> findByOrganizationIdAndIsActiveTrue(Long organizationId, Pageable pageable);

    List<Achievement> findByIsGlobalTrueAndIsActiveTrue();

    @Query("SELECT a FROM Achievement a WHERE a.isActive = true AND " +
           "(a.organizationId = :orgId OR a.isGlobal = true) ORDER BY a.sortOrder")
    List<Achievement> findAllAvailable(@Param("orgId") Long organizationId);

    List<Achievement> findByTypeAndIsActiveTrue(Achievement.AchievementType type);
}
