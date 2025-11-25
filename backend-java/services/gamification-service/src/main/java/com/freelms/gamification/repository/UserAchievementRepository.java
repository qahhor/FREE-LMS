package com.freelms.gamification.repository;

import com.freelms.gamification.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUserId(Long userId);

    Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);

    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.userId = :userId AND ua.isDisplayed = true ORDER BY ua.earnedAt DESC")
    List<UserAchievement> findDisplayedByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.userId = :userId")
    Long countByUser(@Param("userId") Long userId);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.isNotified = false")
    List<UserAchievement> findUnnotified();
}
