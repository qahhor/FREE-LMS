package com.freelms.bot.controller;

import com.freelms.bot.entity.BotConfiguration;
import com.freelms.bot.service.BotService;
import com.freelms.bot.service.SlackService;
import com.freelms.bot.service.TelegramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks", description = "Webhook endpoints for messaging platforms")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final BotService botService;
    private final TelegramService telegramService;
    private final SlackService slackService;

    public WebhookController(BotService botService,
                              TelegramService telegramService,
                              SlackService slackService) {
        this.botService = botService;
        this.telegramService = telegramService;
        this.slackService = slackService;
    }

    // ==================== Telegram Webhooks ====================

    @PostMapping("/telegram/{configId}")
    @Operation(summary = "Telegram webhook endpoint")
    public ResponseEntity<String> handleTelegramWebhook(
            @PathVariable UUID configId,
            @RequestBody Map<String, Object> update) {

        log.debug("Received Telegram webhook for config {}", configId);

        BotConfiguration config = botService.getBotConfiguration(configId);
        if (config.getPlatform() != BotConfiguration.BotPlatform.TELEGRAM) {
            return ResponseEntity.badRequest().body("Invalid platform");
        }

        if (config.getStatus() != BotConfiguration.BotStatus.ACTIVE) {
            return ResponseEntity.ok("Bot inactive");
        }

        try {
            telegramService.handleUpdate(update, config);
        } catch (Exception e) {
            log.error("Error handling Telegram webhook: {}", e.getMessage());
        }

        return ResponseEntity.ok("OK");
    }

    @PostMapping("/telegram/{configId}/setup")
    @Operation(summary = "Setup Telegram webhook")
    public ResponseEntity<Map<String, Object>> setupTelegramWebhook(
            @PathVariable UUID configId,
            @RequestParam String baseUrl) {

        BotConfiguration config = botService.getBotConfiguration(configId);
        String webhookUrl = baseUrl + "/api/v1/webhooks/telegram/" + configId;

        boolean success = telegramService.setWebhook(config.getApiToken(), webhookUrl);

        if (success) {
            config.setWebhookUrl(webhookUrl);
            config.setWebhookVerified(true);
            botService.updateBotConfiguration(configId, config);
        }

        return ResponseEntity.ok(Map.of(
                "success", success,
                "webhookUrl", webhookUrl
        ));
    }

    // ==================== Slack Webhooks ====================

    @PostMapping("/slack/{configId}/events")
    @Operation(summary = "Slack events webhook")
    public ResponseEntity<Map<String, Object>> handleSlackEvents(
            @PathVariable UUID configId,
            @RequestBody Map<String, Object> payload) {

        log.debug("Received Slack event for config {}", configId);

        BotConfiguration config = botService.getBotConfiguration(configId);
        if (config.getPlatform() != BotConfiguration.BotPlatform.SLACK) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid platform"));
        }

        Map<String, Object> response = slackService.handleEvent(payload, config);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/slack/{configId}/interactions")
    @Operation(summary = "Slack interactions webhook")
    public ResponseEntity<Void> handleSlackInteractions(
            @PathVariable UUID configId,
            @RequestParam String payload) {

        log.debug("Received Slack interaction for config {}", configId);

        BotConfiguration config = botService.getBotConfiguration(configId);

        // Parse JSON payload
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> data = mapper.readValue(payload, Map.class);
            slackService.handleInteraction(data, config);
        } catch (Exception e) {
            log.error("Error parsing Slack interaction: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/slack/{configId}/commands")
    @Operation(summary = "Slack slash commands webhook")
    public ResponseEntity<Map<String, Object>> handleSlackCommands(
            @PathVariable UUID configId,
            @RequestParam Map<String, String> params) {

        log.debug("Received Slack command for config {}", configId);

        BotConfiguration config = botService.getBotConfiguration(configId);

        Map<String, Object> response = slackService.handleSlashCommand(params, config);
        return ResponseEntity.ok(response);
    }

    // ==================== WhatsApp Webhooks ====================

    @GetMapping("/whatsapp/{configId}")
    @Operation(summary = "WhatsApp webhook verification")
    public ResponseEntity<String> verifyWhatsAppWebhook(
            @PathVariable UUID configId,
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        BotConfiguration config = botService.getBotConfiguration(configId);

        if ("subscribe".equals(mode) && token.equals(config.getWebhookSecret())) {
            config.setWebhookVerified(true);
            botService.updateBotConfiguration(configId, config);
            return ResponseEntity.ok(challenge);
        }

        return ResponseEntity.status(403).body("Verification failed");
    }

    @PostMapping("/whatsapp/{configId}")
    @Operation(summary = "WhatsApp webhook endpoint")
    public ResponseEntity<String> handleWhatsAppWebhook(
            @PathVariable UUID configId,
            @RequestBody Map<String, Object> payload) {

        log.debug("Received WhatsApp webhook for config {}", configId);

        BotConfiguration config = botService.getBotConfiguration(configId);
        if (config.getPlatform() != BotConfiguration.BotPlatform.WHATSAPP) {
            return ResponseEntity.badRequest().body("Invalid platform");
        }

        // In real implementation: process WhatsApp Cloud API payload
        // Extract messages, statuses, etc.

        return ResponseEntity.ok("OK");
    }

    // ==================== Discord Webhooks ====================

    @PostMapping("/discord/{configId}")
    @Operation(summary = "Discord webhook endpoint")
    public ResponseEntity<Map<String, Object>> handleDiscordWebhook(
            @PathVariable UUID configId,
            @RequestBody Map<String, Object> payload) {

        log.debug("Received Discord webhook for config {}", configId);

        // Handle Discord interaction verification
        Integer type = (Integer) payload.get("type");
        if (type != null && type == 1) {
            // PING - respond with PONG
            return ResponseEntity.ok(Map.of("type", 1));
        }

        BotConfiguration config = botService.getBotConfiguration(configId);

        // In real implementation: process Discord interactions
        // Handle slash commands, button clicks, etc.

        return ResponseEntity.ok(Map.of("type", 4, "data", Map.of("content", "Processing...")));
    }

    // ==================== Microsoft Teams Webhooks ====================

    @PostMapping("/teams/{configId}")
    @Operation(summary = "Microsoft Teams webhook endpoint")
    public ResponseEntity<Map<String, Object>> handleTeamsWebhook(
            @PathVariable UUID configId,
            @RequestBody Map<String, Object> activity) {

        log.debug("Received Teams webhook for config {}", configId);

        BotConfiguration config = botService.getBotConfiguration(configId);
        if (config.getPlatform() != BotConfiguration.BotPlatform.MICROSOFT_TEAMS) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid platform"));
        }

        // In real implementation: process Bot Framework activity
        // Handle messages, adaptive card actions, etc.

        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
