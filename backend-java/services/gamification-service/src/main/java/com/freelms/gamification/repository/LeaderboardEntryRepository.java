package com.freelms.gamification.repository;

import com.freelms.gamification.entity.LeaderboardEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {

    @Query("SELECT le FROM LeaderboardEntry le WHERE le.leaderboard.id = :leaderboardId ORDER BY le.rank ASC")
    Page<LeaderboardEntry> findByLeaderboardOrdered(@Param("leaderboardId") Long leaderboardId, Pageable pageable);

    Optional<LeaderboardEntry> findByLeaderboardIdAndUserId(Long leaderboardId, Long userId);

    @Query("SELECT le FROM LeaderboardEntry le WHERE le.leaderboard.id = :leaderboardId AND le.isTopPerformer = true ORDER BY le.rank ASC")
    Page<LeaderboardEntry> findTopPerformers(@Param("leaderboardId") Long leaderboardId, Pageable pageable);
}
