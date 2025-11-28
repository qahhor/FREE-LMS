package com.freelms.bot.controller;

import com.freelms.bot.entity.BotConfiguration;
import com.freelms.bot.entity.BotUser;
import com.freelms.bot.service.BotService;
import com.freelms.bot.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bots")
@Tag(name = "Bot Platform", description = "Messaging bot management")
public class BotController {

    private final BotService botService;
    private final NotificationService notificationService;

    public BotController(BotService botService, NotificationService notificationService) {
        this.botService = botService;
        this.notificationService = notificationService;
    }

    // ==================== Configuration Endpoints ====================

    @PostMapping("/configurations")
    @Operation(summary = "Create bot configuration")
    public ResponseEntity<BotConfiguration> createConfiguration(@RequestBody BotConfiguration config) {
        return ResponseEntity.ok(botService.createBotConfiguration(config));
    }

    @GetMapping("/configurations/{configId}")
    @Operation(summary = "Get bot configuration")
    public ResponseEntity<BotConfiguration> getConfiguration(@PathVariable UUID configId) {
        return ResponseEntity.ok(botService.getBotConfiguration(configId));
    }

    @PutMapping("/configurations/{configId}")
    @Operation(summary = "Update bot configuration")
    public ResponseEntity<BotConfiguration> updateConfiguration(
            @PathVariable UUID configId,
            @RequestBody BotConfiguration updates) {
        return ResponseEntity.ok(botService.updateBotConfiguration(configId, updates));
    }

    @GetMapping("/configurations")
    @Operation(summary = "List bot configurations")
    public ResponseEntity<List<BotConfiguration>> listConfigurations(
            @RequestParam(required = false) Long organizationId) {
        return ResponseEntity.ok(botService.getBotConfigurations(organizationId));
    }

    @PatchMapping("/configurations/{configId}/status")
    @Operation(summary = "Update bot status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID configId,
            @RequestParam BotConfiguration.BotStatus status) {
        botService.updateBotStatus(configId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/configurations/{configId}/stats")
    @Operation(summary = "Get bot statistics")
    public ResponseEntity<Map<String, Object>> getStats(
            @PathVariable UUID configId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(botService.getBotStats(configId, days));
    }

    // ==================== User Endpoints ====================

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get bot user")
    public ResponseEntity<BotUser> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(botService.getBotUser(userId));
    }

    @GetMapping("/users/lms/{lmsUserId}")
    @Operation(summary = "Get bot users for LMS user")
    public ResponseEntity<List<BotUser>> getUsersByLmsId(@PathVariable Long lmsUserId) {
        return ResponseEntity.ok(botService.getLmsUserBots(lmsUserId));
    }

    @PostMapping("/users/{userId}/link")
    @Operation(summary = "Link bot user to LMS user")
    public ResponseEntity<BotUser> linkUser(
            @PathVariable UUID userId,
            @RequestParam Long lmsUserId) {
        return ResponseEntity.ok(botService.linkToLmsUser(userId, lmsUserId));
    }

    @PostMapping("/users/{userId}/verify")
    @Operation(summary = "Generate verification code")
    public ResponseEntity<Map<String, String>> generateVerificationCode(@PathVariable UUID userId) {
        String code = botService.generateVerificationCode(userId);
        return ResponseEntity.ok(Map.of("code", code));
    }

    @PostMapping("/users/verify")
    @Operation(summary = "Verify user with code")
    public ResponseEntity<BotUser> verifyUser(
            @RequestParam String code,
            @RequestParam Long lmsUserId) {
        return ResponseEntity.ok(botService.verifyUser(code, lmsUserId));
    }

    @PutMapping("/users/{userId}/preferences")
    @Operation(summary = "Update user preferences")
    public ResponseEntity<Void> updatePreferences(
            @PathVariable UUID userId,
            @RequestBody Map<String, Boolean> preferences) {
        botService.updateUserPreferences(userId, preferences);
        return ResponseEntity.ok().build();
    }

    // ==================== Notification Endpoints ====================

    @PostMapping("/notify/{lmsUserId}")
    @Operation(summary = "Send notification to user")
    public ResponseEntity<Void> notifyUser(
            @PathVariable Long lmsUserId,
            @RequestBody Map<String, Object> notification) {
        String type = (String) notification.get("type");
        String message = (String) notification.get("message");

        notificationService.notifyUser(
                lmsUserId,
                com.freelms.bot.entity.BotMessage.NotificationType.valueOf(type),
                message
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/notify/course-update")
    @Operation(summary = "Send course update notification")
    public ResponseEntity<Void> notifyCourseUpdate(@RequestBody Map<String, Object> data) {
        notificationService.notifyCourseUpdate(
                ((Number) data.get("userId")).longValue(),
                ((Number) data.get("courseId")).longValue(),
                (String) data.get("courseName"),
                (String) data.get("message")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notify/assignment-due")
    @Operation(summary = "Send assignment due notification")
    public ResponseEntity<Void> notifyAssignmentDue(@RequestBody Map<String, Object> data) {
        notificationService.notifyAssignmentDue(
                ((Number) data.get("userId")).longValue(),
                (String) data.get("assignmentName"),
                (String) data.get("courseName"),
                (String) data.get("dueDate")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notify/grade-posted")
    @Operation(summary = "Send grade notification")
    public ResponseEntity<Void> notifyGradePosted(@RequestBody Map<String, Object> data) {
        notificationService.notifyGradePosted(
                ((Number) data.get("userId")).longValue(),
                (String) data.get("itemName"),
                (String) data.get("courseName"),
                (String) data.get("grade"),
                (String) data.get("score")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notify/event-reminder")
    @Operation(summary = "Send event reminder")
    public ResponseEntity<Void> notifyEventReminder(@RequestBody Map<String, Object> data) {
        notificationService.notifyEventReminder(
                ((Number) data.get("userId")).longValue(),
                (String) data.get("eventName"),
                (String) data.get("dateTime"),
                (String) data.get("location")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notify/certificate-ready")
    @Operation(summary = "Send certificate notification")
    public ResponseEntity<Void> notifyCertificateReady(@RequestBody Map<String, Object> data) {
        notificationService.notifyCertificateReady(
                ((Number) data.get("userId")).longValue(),
                (String) data.get("courseName"),
                (String) data.get("certificateUrl")
        );
        return ResponseEntity.ok().build();
    }
}
