package com.freelms.gamification.controller;

import com.freelms.common.dto.ApiResponse;
import com.freelms.common.security.UserPrincipal;
import com.freelms.gamification.dto.*;
import com.freelms.gamification.entity.Leaderboard;
import com.freelms.gamification.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    // Points
    @GetMapping("/points/my")
    public ResponseEntity<ApiResponse<UserPointsDto>> getMyPoints(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserPointsDto result = gamificationService.addPoints(principal.getId(), principal.getOrganizationId(), 0, "fetch", null);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/points/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserPointsDto>> addPoints(
            @RequestParam Long userId,
            @RequestParam int points,
            @RequestParam String source,
            @RequestParam(required = false) Long sourceId,
            @AuthenticationPrincipal UserPrincipal principal) {

        UserPointsDto result = gamificationService.addPoints(userId, principal.getOrganizationId(), points, source, sourceId);
        return ResponseEntity.ok(ApiResponse.success(result, "Points added successfully"));
    }

    // Achievements
    @GetMapping("/achievements")
    public ResponseEntity<ApiResponse<List<AchievementDto>>> getMyAchievements(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<AchievementDto> result = gamificationService.getUserAchievements(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/achievements/user/{userId}")
    public ResponseEntity<ApiResponse<List<AchievementDto>>> getUserAchievements(@PathVariable Long userId) {
        List<AchievementDto> result = gamificationService.getUserAchievements(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/achievements/{achievementId}/award")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AchievementDto>> awardAchievement(
            @PathVariable Long achievementId,
            @RequestParam Long userId) {

        AchievementDto result = gamificationService.awardAchievement(userId, achievementId);
        return ResponseEntity.ok(ApiResponse.success(result, "Achievement awarded"));
    }

    // Streaks
    @GetMapping("/streaks/my")
    public ResponseEntity<ApiResponse<UserStreakDto>> getMyStreak(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserStreakDto result = gamificationService.recordActivity(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/streaks/record")
    public ResponseEntity<ApiResponse<UserStreakDto>> recordActivity(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserStreakDto result = gamificationService.recordActivity(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "Activity recorded"));
    }

    // Leaderboards
    @GetMapping("/leaderboards")
    public ResponseEntity<ApiResponse<LeaderboardDto>> getLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "WEEKLY") Leaderboard.PeriodType periodType,
            @RequestParam(defaultValue = "POINTS") Leaderboard.LeaderboardType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LeaderboardDto result = gamificationService.getLeaderboard(
                principal.getOrganizationId(), periodType, type, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/leaderboards/weekly")
    public ResponseEntity<ApiResponse<LeaderboardDto>> getWeeklyLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LeaderboardDto result = gamificationService.getLeaderboard(
                principal.getOrganizationId(), Leaderboard.PeriodType.WEEKLY, Leaderboard.LeaderboardType.POINTS, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/leaderboards/monthly")
    public ResponseEntity<ApiResponse<LeaderboardDto>> getMonthlyLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LeaderboardDto result = gamificationService.getLeaderboard(
                principal.getOrganizationId(), Leaderboard.PeriodType.MONTHLY, Leaderboard.LeaderboardType.POINTS, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/leaderboards/quarterly")
    public ResponseEntity<ApiResponse<LeaderboardDto>> getQuarterlyLeaderboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LeaderboardDto result = gamificationService.getLeaderboard(
                principal.getOrganizationId(), Leaderboard.PeriodType.QUARTERLY, Leaderboard.LeaderboardType.POINTS, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
