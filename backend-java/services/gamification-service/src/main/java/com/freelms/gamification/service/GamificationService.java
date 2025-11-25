package com.freelms.gamification.service;

import com.freelms.common.exception.ResourceNotFoundException;
import com.freelms.gamification.dto.*;
import com.freelms.gamification.entity.*;
import com.freelms.gamification.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GamificationService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserPointsRepository userPointsRepository;
    private final UserStreakRepository userStreakRepository;
    private final ChallengeRepository challengeRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Points Management
    public UserPointsDto addPoints(Long userId, Long organizationId, int points, String source, Long sourceId) {
        UserPoints userPoints = userPointsRepository.findByUserId(userId)
                .orElse(UserPoints.builder()
                        .userId(userId)
                        .organizationId(organizationId)
                        .build());

        userPoints.setTotalPoints(userPoints.getTotalPoints() + points);
        userPoints.setWeeklyPoints(userPoints.getWeeklyPoints() + points);
        userPoints.setMonthlyPoints(userPoints.getMonthlyPoints() + points);
        userPoints.setQuarterlyPoints(userPoints.getQuarterlyPoints() + points);
        userPoints.setYearlyPoints(userPoints.getYearlyPoints() + points);
        userPoints.setExperiencePoints(userPoints.getExperiencePoints() + points);

        // Level calculation
        int newLevel = calculateLevel(userPoints.getExperiencePoints());
        if (newLevel > userPoints.getLevel()) {
            userPoints.setLevel(newLevel);
            kafkaTemplate.send("gamification-events", "level-up",
                    new LevelUpEvent(userId, newLevel));
        }

        UserPoints saved = userPointsRepository.save(userPoints);
        log.info("Added {} points to user {}, source: {}", points, userId, source);

        return mapUserPointsToDto(saved);
    }

    public UserPointsDto addCoins(Long userId, int coins, String source) {
        UserPoints userPoints = userPointsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User points not found: " + userId));

        userPoints.setCoinsBalance(userPoints.getCoinsBalance() + coins);
        UserPoints saved = userPointsRepository.save(userPoints);

        log.info("Added {} coins to user {}, source: {}", coins, userId, source);
        return mapUserPointsToDto(saved);
    }

    private int calculateLevel(Long xp) {
        // Simple level formula: level = sqrt(xp / 100)
        return (int) Math.floor(Math.sqrt(xp / 100.0)) + 1;
    }

    // Achievements
    public AchievementDto awardAchievement(Long userId, Long achievementId) {
        if (userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId)) {
            throw new IllegalStateException("User already has this achievement");
        }

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found: " + achievementId));

        UserAchievement userAchievement = UserAchievement.builder()
                .userId(userId)
                .achievement(achievement)
                .earnedAt(LocalDateTime.now())
                .progress(100)
                .isNotified(false)
                .isDisplayed(true)
                .build();

        userAchievementRepository.save(userAchievement);

        // Award points and coins
        if (achievement.getPointsReward() > 0) {
            addPoints(userId, achievement.getOrganizationId(), achievement.getPointsReward(), "achievement", achievementId);
        }
        if (achievement.getCoinsReward() > 0) {
            addCoins(userId, achievement.getCoinsReward(), "achievement");
        }

        kafkaTemplate.send("gamification-events", "achievement-earned",
                new AchievementEvent(userId, achievementId, achievement.getName()));

        log.info("Awarded achievement {} to user {}", achievement.getName(), userId);
        return mapAchievementToDto(achievement);
    }

    public List<AchievementDto> getUserAchievements(Long userId) {
        return userAchievementRepository.findByUserId(userId).stream()
                .map(ua -> mapAchievementToDto(ua.getAchievement()))
                .collect(Collectors.toList());
    }

    // Streaks
    public UserStreakDto recordActivity(Long userId) {
        LocalDate today = LocalDate.now();

        UserStreak streak = userStreakRepository.findByUserId(userId)
                .orElse(UserStreak.builder()
                        .userId(userId)
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());

        if (streak.getLastActivityDate() == null || streak.getLastActivityDate().isBefore(today)) {
            if (streak.getLastActivityDate() != null && streak.getLastActivityDate().plusDays(1).equals(today)) {
                // Consecutive day
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            } else if (streak.getLastActivityDate() == null || streak.getLastActivityDate().plusDays(2).isBefore(today)) {
                // Streak broken (unless freeze)
                if (!streak.getFreezeUsedToday() && streak.getFreezeCount() > 0) {
                    streak.setFreezeCount(streak.getFreezeCount() - 1);
                    streak.setFreezeUsedToday(true);
                } else {
                    streak.setCurrentStreak(1);
                    streak.setStreakStartDate(today);
                }
            }

            streak.setLastActivityDate(today);
            streak.setTotalActiveDays(streak.getTotalActiveDays() + 1);
            streak.setWeeklyProgress(streak.getWeeklyProgress() + 1);

            if (streak.getCurrentStreak() > streak.getLongestStreak()) {
                streak.setLongestStreak(streak.getCurrentStreak());
            }

            // Check streak milestones
            checkStreakMilestones(userId, streak.getCurrentStreak());
        }

        UserStreak saved = userStreakRepository.save(streak);
        log.info("Recorded activity for user {}, current streak: {}", userId, saved.getCurrentStreak());

        return mapStreakToDto(saved);
    }

    private void checkStreakMilestones(Long userId, int streak) {
        int[] milestones = {7, 14, 30, 60, 90, 180, 365};
        for (int milestone : milestones) {
            if (streak == milestone) {
                // Award streak achievement
                kafkaTemplate.send("gamification-events", "streak-milestone",
                        new StreakMilestoneEvent(userId, milestone));
            }
        }
    }

    // Leaderboards
    public LeaderboardDto getLeaderboard(Long organizationId, Leaderboard.PeriodType periodType,
                                          Leaderboard.LeaderboardType type, int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        Leaderboard leaderboard = leaderboardRepository.findCurrentLeaderboard(organizationId, periodType, type, now)
                .orElseGet(() -> createLeaderboard(organizationId, periodType, type));

        Page<LeaderboardEntry> entries = leaderboardEntryRepository.findByLeaderboardOrdered(
                leaderboard.getId(), PageRequest.of(page, size));

        return LeaderboardDto.builder()
                .id(leaderboard.getId())
                .periodType(periodType)
                .leaderboardType(type)
                .periodStart(leaderboard.getPeriodStart())
                .periodEnd(leaderboard.getPeriodEnd())
                .entries(entries.getContent().stream()
                        .map(this::mapEntryToDto)
                        .collect(Collectors.toList()))
                .totalEntries(entries.getTotalElements())
                .build();
    }

    private Leaderboard createLeaderboard(Long organizationId, Leaderboard.PeriodType periodType,
                                           Leaderboard.LeaderboardType type) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start, end;

        switch (periodType) {
            case WEEKLY -> {
                start = now.toLocalDate().atStartOfDay().minusDays(now.getDayOfWeek().getValue() - 1);
                end = start.plusWeeks(1);
            }
            case MONTHLY -> {
                start = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
                end = start.plusMonths(1);
            }
            case QUARTERLY -> {
                int quarter = (now.getMonthValue() - 1) / 3;
                start = now.toLocalDate().withMonth(quarter * 3 + 1).withDayOfMonth(1).atStartOfDay();
                end = start.plusMonths(3);
            }
            default -> {
                start = now.toLocalDate().withDayOfYear(1).atStartOfDay();
                end = start.plusYears(1);
            }
        }

        Leaderboard leaderboard = Leaderboard.builder()
                .organizationId(organizationId)
                .periodType(periodType)
                .leaderboardType(type)
                .periodStart(start)
                .periodEnd(end)
                .isFinalized(false)
                .build();

        return leaderboardRepository.save(leaderboard);
    }

    @Scheduled(cron = "0 0 0 * * MON") // Every Monday at midnight
    public void resetWeeklyLeaderboards() {
        userPointsRepository.resetWeeklyPoints();
        userStreakRepository.resetWeeklyProgress();
        log.info("Reset weekly leaderboards");
    }

    @Scheduled(cron = "0 0 0 1 * *") // First day of month
    public void resetMonthlyLeaderboards() {
        userPointsRepository.resetMonthlyPoints();
        log.info("Reset monthly leaderboards");
    }

    // Event Listeners
    @KafkaListener(topics = "course-events", groupId = "gamification-service")
    public void handleCourseEvent(Object event) {
        log.debug("Received course event: {}", event);
        // Process course completion, quiz scores, etc.
    }

    @KafkaListener(topics = "learning-path-events", groupId = "gamification-service")
    public void handleLearningPathEvent(Object event) {
        log.debug("Received learning path event: {}", event);
        // Process path completion
    }

    // Mappers
    private UserPointsDto mapUserPointsToDto(UserPoints entity) {
        return UserPointsDto.builder()
                .userId(entity.getUserId())
                .totalPoints(entity.getTotalPoints())
                .weeklyPoints(entity.getWeeklyPoints())
                .monthlyPoints(entity.getMonthlyPoints())
                .quarterlyPoints(entity.getQuarterlyPoints())
                .level(entity.getLevel())
                .experiencePoints(entity.getExperiencePoints())
                .coinsBalance(entity.getCoinsBalance())
                .build();
    }

    private AchievementDto mapAchievementToDto(Achievement entity) {
        return AchievementDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .iconUrl(entity.getIconUrl())
                .type(entity.getType())
                .rarity(entity.getRarity())
                .pointsReward(entity.getPointsReward())
                .coinsReward(entity.getCoinsReward())
                .isHidden(entity.getIsHidden())
                .build();
    }

    private UserStreakDto mapStreakToDto(UserStreak entity) {
        return UserStreakDto.builder()
                .userId(entity.getUserId())
                .currentStreak(entity.getCurrentStreak())
                .longestStreak(entity.getLongestStreak())
                .lastActivityDate(entity.getLastActivityDate())
                .freezeCount(entity.getFreezeCount())
                .weeklyGoal(entity.getWeeklyGoal())
                .weeklyProgress(entity.getWeeklyProgress())
                .totalActiveDays(entity.getTotalActiveDays())
                .build();
    }

    private LeaderboardEntryDto mapEntryToDto(LeaderboardEntry entry) {
        return LeaderboardEntryDto.builder()
                .userId(entry.getUserId())
                .score(entry.getScore())
                .rank(entry.getRank())
                .previousRank(entry.getPreviousRank())
                .rankChange(entry.getRankChange())
                .isTopPerformer(entry.getIsTopPerformer())
                .build();
    }

    // Events
    public record LevelUpEvent(Long userId, int newLevel) {}
    public record AchievementEvent(Long userId, Long achievementId, String achievementName) {}
    public record StreakMilestoneEvent(Long userId, int streakDays) {}
}
