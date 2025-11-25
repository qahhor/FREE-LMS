package com.freelms.gamification.repository;

import com.freelms.gamification.entity.UserPoints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointsRepository extends JpaRepository<UserPoints, Long> {

    Optional<UserPoints> findByUserId(Long userId);

    @Query("SELECT up FROM UserPoints up WHERE up.organizationId = :orgId ORDER BY up.totalPoints DESC")
    Page<UserPoints> findTopByTotalPoints(@Param("orgId") Long organizationId, Pageable pageable);

    @Query("SELECT up FROM UserPoints up WHERE up.organizationId = :orgId ORDER BY up.weeklyPoints DESC")
    Page<UserPoints> findTopByWeeklyPoints(@Param("orgId") Long organizationId, Pageable pageable);

    @Query("SELECT up FROM UserPoints up WHERE up.organizationId = :orgId ORDER BY up.monthlyPoints DESC")
    Page<UserPoints> findTopByMonthlyPoints(@Param("orgId") Long organizationId, Pageable pageable);

    @Query("SELECT up FROM UserPoints up WHERE up.organizationId = :orgId ORDER BY up.quarterlyPoints DESC")
    Page<UserPoints> findTopByQuarterlyPoints(@Param("orgId") Long organizationId, Pageable pageable);

    @Modifying
    @Query("UPDATE UserPoints up SET up.weeklyPoints = 0")
    void resetWeeklyPoints();

    @Modifying
    @Query("UPDATE UserPoints up SET up.monthlyPoints = 0")
    void resetMonthlyPoints();

    @Modifying
    @Query("UPDATE UserPoints up SET up.quarterlyPoints = 0")
    void resetQuarterlyPoints();
}
