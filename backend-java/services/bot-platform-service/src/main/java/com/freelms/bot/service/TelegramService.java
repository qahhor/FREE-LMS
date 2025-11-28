package com.freelms.bot.service;

import com.freelms.bot.entity.BotConfiguration;
import com.freelms.bot.entity.BotMessage;
import com.freelms.bot.entity.BotUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Telegram Bot API integration service.
 */
@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";

    private final RestTemplate restTemplate;
    private final BotService botService;

    public TelegramService(BotService botService) {
        this.restTemplate = new RestTemplate();
        this.botService = botService;
    }

    /**
     * Send a text message.
     */
    public void sendMessage(String chatId, String text, List<Map<String, String>> buttons) {
        // In real implementation, get token from config
        String token = "BOT_TOKEN";

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        payload.put("parse_mode", "Markdown");

        if (buttons != null && !buttons.isEmpty()) {
            List<List<Map<String, Object>>> keyboard = new ArrayList<>();
            List<Map<String, Object>> row = new ArrayList<>();

            for (Map<String, String> button : buttons) {
                Map<String, Object> btn = new HashMap<>();
                btn.put("text", button.get("text"));

                if (button.containsKey("url")) {
                    btn.put("url", button.get("url"));
                } else if (button.containsKey("callback_data")) {
                    btn.put("callback_data", button.get("callback_data"));
                }

                row.add(btn);
            }
            keyboard.add(row);

            payload.put("reply_markup", Map.of("inline_keyboard", keyboard));
        }

        try {
            String url = TELEGRAM_API_BASE + token + "/sendMessage";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, request, Map.class);

            log.debug("Sent Telegram message to chat {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage());
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }

    /**
     * Send a photo with caption.
     */
    public void sendPhoto(String chatId, String photoUrl, String caption) {
        String token = "BOT_TOKEN";

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("photo", photoUrl);
        payload.put("caption", caption);
        payload.put("parse_mode", "Markdown");

        try {
            String url = TELEGRAM_API_BASE + token + "/sendPhoto";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, request, Map.class);
        } catch (Exception e) {
            log.error("Failed to send Telegram photo: {}", e.getMessage());
        }
    }

    /**
     * Send a document.
     */
    public void sendDocument(String chatId, String documentUrl, String caption) {
        String token = "BOT_TOKEN";

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("document", documentUrl);
        payload.put("caption", caption);

        try {
            String url = TELEGRAM_API_BASE + token + "/sendDocument";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, request, Map.class);
        } catch (Exception e) {
            log.error("Failed to send Telegram document: {}", e.getMessage());
        }
    }

    /**
     * Handle incoming webhook update.
     */
    public void handleUpdate(Map<String, Object> update, BotConfiguration config) {
        log.debug("Received Telegram update: {}", update);

        // Extract message
        Map<String, Object> message = (Map<String, Object>) update.get("message");
        if (message == null) {
            // Check for callback query
            Map<String, Object> callbackQuery = (Map<String, Object>) update.get("callback_query");
            if (callbackQuery != null) {
                handleCallbackQuery(callbackQuery, config);
            }
            return;
        }

        // Extract chat and user info
        Map<String, Object> chat = (Map<String, Object>) message.get("chat");
        Map<String, Object> from = (Map<String, Object>) message.get("from");

        String chatId = String.valueOf(chat.get("id"));
        String platformUserId = String.valueOf(from.get("id"));
        String username = (String) from.get("username");
        String firstName = (String) from.get("first_name");
        String lastName = (String) from.get("last_name");
        String displayName = firstName + (lastName != null ? " " + lastName : "");

        // Register or update user
        BotUser user = botService.registerUser(
                BotConfiguration.BotPlatform.TELEGRAM,
                platformUserId,
                chatId,
                username,
                displayName
        );

        // Process message text
        String text = (String) message.get("text");
        if (text != null) {
            if (text.startsWith("/")) {
                handleCommand(user, config, text);
            } else {
                handleTextMessage(user, config, text);
            }
        }
    }

    private void handleCommand(BotUser user, BotConfiguration config, String text) {
        String[] parts = text.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "/start" -> handleStartCommand(user, config);
            case "/help" -> handleHelpCommand(user, config);
            case "/link" -> handleLinkCommand(user, config, args);
            case "/settings" -> handleSettingsCommand(user, config);
            case "/courses" -> handleCoursesCommand(user, config);
            case "/grades" -> handleGradesCommand(user, config);
            case "/notifications" -> handleNotificationsCommand(user, config);
            default -> sendMessage(user.getChatId(), "Unknown command. Type /help for available commands.", null);
        }

        // Log command
        botService.logInboundMessage(user, config, text, BotMessage.MessageType.COMMAND);
    }

    private void handleStartCommand(BotUser user, BotConfiguration config) {
        String message = """
                👋 *Welcome to FREE LMS Bot!*

                I can help you stay updated with your courses, assignments, and grades.

                To get started, link your LMS account:
                1. Go to your LMS profile settings
                2. Click "Connect Telegram"
                3. Enter the code shown here

                Or type `/link YOUR_EMAIL` to receive a verification code.

                Type /help for all available commands.
                """;

        sendMessage(user.getChatId(), message, List.of(
                Map.of("text", "🔗 Link Account", "callback_data", "link_account"),
                Map.of("text", "❓ Help", "callback_data", "help")
        ));
    }

    private void handleHelpCommand(BotUser user, BotConfiguration config) {
        String message = """
                📖 *Available Commands*

                /start - Start the bot
                /help - Show this help message
                /link - Link your LMS account
                /courses - View your courses
                /grades - Check recent grades
                /settings - Notification settings
                /notifications - Toggle notifications

                *Need help?*
                Contact support at support@freelms.com
                """;

        sendMessage(user.getChatId(), message, null);
    }

    private void handleLinkCommand(BotUser user, BotConfiguration config, String args) {
        if (user.getVerified()) {
            sendMessage(user.getChatId(), "✅ Your account is already linked!", null);
            return;
        }

        String code = botService.generateVerificationCode(user.getId());
        String message = String.format("""
                🔐 *Verification Code*

                Your code: `%s`

                Enter this code in your LMS profile to complete linking.
                The code expires in 15 minutes.
                """, code);

        sendMessage(user.getChatId(), message, null);
    }

    private void handleSettingsCommand(BotUser user, BotConfiguration config) {
        if (!user.getVerified()) {
            sendMessage(user.getChatId(), "Please link your account first with /link", null);
            return;
        }

        String status = user.getNotificationsEnabled() ? "✅ ON" : "❌ OFF";
        String message = String.format("""
                ⚙️ *Notification Settings*

                Notifications: %s

                • Course updates: %s
                • Assignment reminders: %s
                • Grade notifications: %s
                • Event reminders: %s
                """,
                status,
                user.getCourseUpdates() ? "✅" : "❌",
                user.getAssignmentReminders() ? "✅" : "❌",
                user.getGradeNotifications() ? "✅" : "❌",
                user.getEventReminders() ? "✅" : "❌"
        );

        sendMessage(user.getChatId(), message, List.of(
                Map.of("text", "Toggle All", "callback_data", "toggle_notifications"),
                Map.of("text", "Customize", "callback_data", "customize_notifications")
        ));
    }

    private void handleCoursesCommand(BotUser user, BotConfiguration config) {
        if (!user.getVerified()) {
            sendMessage(user.getChatId(), "Please link your account first with /link", null);
            return;
        }

        // In real implementation, fetch from course-service
        sendMessage(user.getChatId(), "📚 Fetching your courses...", null);
    }

    private void handleGradesCommand(BotUser user, BotConfiguration config) {
        if (!user.getVerified()) {
            sendMessage(user.getChatId(), "Please link your account first with /link", null);
            return;
        }

        // In real implementation, fetch from grade-service
        sendMessage(user.getChatId(), "📊 Fetching your grades...", null);
    }

    private void handleNotificationsCommand(BotUser user, BotConfiguration config) {
        boolean newState = !user.getNotificationsEnabled();
        botService.updateUserPreferences(user.getId(), Map.of("notificationsEnabled", newState));

        String message = newState ?
                "🔔 Notifications enabled!" :
                "🔕 Notifications disabled.";

        sendMessage(user.getChatId(), message, null);
    }

    private void handleTextMessage(BotUser user, BotConfiguration config, String text) {
        // Log the message
        botService.logInboundMessage(user, config, text, BotMessage.MessageType.TEXT);

        // Simple response for non-command messages
        if (!user.getVerified()) {
            sendMessage(user.getChatId(),
                    "Please link your account first using /link to access all features.", null);
        } else {
            sendMessage(user.getChatId(),
                    "I received your message. Use /help to see available commands.", null);
        }
    }

    private void handleCallbackQuery(Map<String, Object> callbackQuery, BotConfiguration config) {
        String data = (String) callbackQuery.get("data");
        Map<String, Object> from = (Map<String, Object>) callbackQuery.get("from");
        String platformUserId = String.valueOf(from.get("id"));

        botService.findBotUser(BotConfiguration.BotPlatform.TELEGRAM, platformUserId)
                .ifPresent(user -> {
                    switch (data) {
                        case "link_account" -> handleLinkCommand(user, config, "");
                        case "help" -> handleHelpCommand(user, config);
                        case "toggle_notifications" -> handleNotificationsCommand(user, config);
                        case "customize_notifications" -> handleSettingsCommand(user, config);
                    }
                });
    }

    /**
     * Set webhook URL for Telegram bot.
     */
    public boolean setWebhook(String token, String webhookUrl) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("url", webhookUrl);
        payload.put("allowed_updates", List.of("message", "callback_query"));

        try {
            String url = TELEGRAM_API_BASE + token + "/setWebhook";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to set Telegram webhook: {}", e.getMessage());
            return false;
        }
    }
}
