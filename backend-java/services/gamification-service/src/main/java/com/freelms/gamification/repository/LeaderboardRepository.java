package com.freelms.gamification.repository;

import com.freelms.gamification.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {

    @Query("SELECT l FROM Leaderboard l WHERE l.organizationId = :orgId " +
           "AND l.periodType = :periodType AND l.leaderboardType = :type " +
           "AND l.periodStart <= :date AND l.periodEnd > :date")
    Optional<Leaderboard> findCurrentLeaderboard(@Param("orgId") Long organizationId,
                                                   @Param("periodType") Leaderboard.PeriodType periodType,
                                                   @Param("type") Leaderboard.LeaderboardType type,
                                                   @Param("date") LocalDateTime date);

    @Query("SELECT l FROM Leaderboard l WHERE l.organizationId = :orgId " +
           "AND l.departmentId = :deptId AND l.periodType = :periodType " +
           "AND l.periodStart <= :date AND l.periodEnd > :date")
    Optional<Leaderboard> findDepartmentLeaderboard(@Param("orgId") Long organizationId,
                                                     @Param("deptId") Long departmentId,
                                                     @Param("periodType") Leaderboard.PeriodType periodType,
                                                     @Param("date") LocalDateTime date);
}
