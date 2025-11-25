package com.freelms.gamification.repository;

import com.freelms.gamification.entity.UserStreak;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {

    Optional<UserStreak> findByUserId(Long userId);

    @Query("SELECT us FROM UserStreak us ORDER BY us.currentStreak DESC")
    Page<UserStreak> findTopStreaks(Pageable pageable);

    @Query("SELECT us FROM UserStreak us ORDER BY us.longestStreak DESC")
    Page<UserStreak> findTopLongestStreaks(Pageable pageable);

    @Query("SELECT us FROM UserStreak us WHERE us.lastActivityDate < :date AND us.currentStreak > 0")
    List<UserStreak> findStreaksToReset(@Param("date") LocalDate date);

    @Modifying
    @Query("UPDATE UserStreak us SET us.freezeUsedToday = false")
    void resetDailyFreezeFlags();

    @Modifying
    @Query("UPDATE UserStreak us SET us.weeklyProgress = 0")
    void resetWeeklyProgress();
}
