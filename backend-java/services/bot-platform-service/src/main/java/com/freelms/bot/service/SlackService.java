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
 * Slack Bot API integration service.
 */
@Service
public class SlackService {

    private static final Logger log = LoggerFactory.getLogger(SlackService.class);
    private static final String SLACK_API_BASE = "https://slack.com/api/";

    private final RestTemplate restTemplate;
    private final BotService botService;

    public SlackService(BotService botService) {
        this.restTemplate = new RestTemplate();
        this.botService = botService;
    }

    /**
     * Send a message to a Slack channel or DM.
     */
    public void sendMessage(String userId, String channelId, String text, List<Map<String, String>> buttons) {
        String token = "xoxb-BOT_TOKEN";

        List<Map<String, Object>> blocks = new ArrayList<>();

        // Text block
        blocks.add(Map.of(
                "type", "section",
                "text", Map.of(
                        "type", "mrkdwn",
                        "text", text
                )
        ));

        // Add buttons if present
        if (buttons != null && !buttons.isEmpty()) {
            List<Map<String, Object>> elements = new ArrayList<>();
            for (Map<String, String> button : buttons) {
                Map<String, Object> btn = new HashMap<>();
                btn.put("type", "button");
                btn.put("text", Map.of("type", "plain_text", "text", button.get("text")));

                if (button.containsKey("url")) {
                    btn.put("url", button.get("url"));
                } else if (button.containsKey("action_id")) {
                    btn.put("action_id", button.get("action_id"));
                    btn.put("value", button.getOrDefault("value", "click"));
                }

                elements.add(btn);
            }

            blocks.add(Map.of(
                    "type", "actions",
                    "elements", elements
            ));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("channel", channelId != null ? channelId : userId);
        payload.put("blocks", blocks);
        payload.put("text", text);  // Fallback text

        try {
            String url = SLACK_API_BASE + "chat.postMessage";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getBody() != null && !(Boolean) response.getBody().get("ok")) {
                log.error("Slack API error: {}", response.getBody().get("error"));
            }
        } catch (Exception e) {
            log.error("Failed to send Slack message: {}", e.getMessage());
            throw new RuntimeException("Failed to send Slack message", e);
        }
    }

    /**
     * Send a message with rich formatting.
     */
    public void sendRichMessage(String channelId, String title, String text,
                                 String color, List<Map<String, String>> fields) {
        String token = "xoxb-BOT_TOKEN";

        List<Map<String, Object>> attachments = new ArrayList<>();
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", color != null ? color : "#36a64f");
        attachment.put("title", title);
        attachment.put("text", text);
        attachment.put("mrkdwn_in", List.of("text", "fields"));

        if (fields != null && !fields.isEmpty()) {
            List<Map<String, Object>> attachmentFields = new ArrayList<>();
            for (Map<String, String> field : fields) {
                attachmentFields.add(Map.of(
                        "title", field.get("title"),
                        "value", field.get("value"),
                        "short", Boolean.parseBoolean(field.getOrDefault("short", "true"))
                ));
            }
            attachment.put("fields", attachmentFields);
        }

        attachments.add(attachment);

        Map<String, Object> payload = new HashMap<>();
        payload.put("channel", channelId);
        payload.put("attachments", attachments);

        try {
            String url = SLACK_API_BASE + "chat.postMessage";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, request, Map.class);
        } catch (Exception e) {
            log.error("Failed to send Slack rich message: {}", e.getMessage());
        }
    }

    /**
     * Handle incoming Slack event.
     */
    public Map<String, Object> handleEvent(Map<String, Object> payload, BotConfiguration config) {
        String type = (String) payload.get("type");

        // Handle URL verification challenge
        if ("url_verification".equals(type)) {
            return Map.of("challenge", payload.get("challenge"));
        }

        // Handle events
        if ("event_callback".equals(type)) {
            Map<String, Object> event = (Map<String, Object>) payload.get("event");
            String eventType = (String) event.get("type");

            switch (eventType) {
                case "message" -> handleMessage(event, config);
                case "app_mention" -> handleMention(event, config);
                case "app_home_opened" -> handleAppHomeOpened(event, config);
            }
        }

        return Map.of("ok", true);
    }

    /**
     * Handle incoming Slack interaction (button clicks, etc.).
     */
    public void handleInteraction(Map<String, Object> payload, BotConfiguration config) {
        String type = (String) payload.get("type");

        if ("block_actions".equals(type)) {
            List<Map<String, Object>> actions = (List<Map<String, Object>>) payload.get("actions");
            Map<String, Object> user = (Map<String, Object>) payload.get("user");
            String userId = (String) user.get("id");

            for (Map<String, Object> action : actions) {
                String actionId = (String) action.get("action_id");
                String value = (String) action.get("value");

                handleAction(userId, actionId, value, config);
            }
        }
    }

    /**
     * Handle slash command.
     */
    public Map<String, Object> handleSlashCommand(Map<String, String> params, BotConfiguration config) {
        String command = params.get("command");
        String text = params.get("text");
        String userId = params.get("user_id");
        String channelId = params.get("channel_id");

        // Register/update user
        BotUser user = botService.registerUser(
                BotConfiguration.BotPlatform.SLACK,
                userId,
                userId,
                params.get("user_name"),
                params.get("user_name")
        );
        user.setChannelId(channelId);

        String response = switch (command) {
            case "/lms" -> handleLmsCommand(user, text);
            case "/courses" -> handleCoursesSlashCommand(user);
            case "/grades" -> handleGradesSlashCommand(user);
            default -> "Unknown command. Try `/lms help` for available commands.";
        };

        return Map.of(
                "response_type", "ephemeral",
                "text", response
        );
    }

    private void handleMessage(Map<String, Object> event, BotConfiguration config) {
        // Ignore bot messages
        if (event.containsKey("bot_id")) return;

        String userId = (String) event.get("user");
        String channelId = (String) event.get("channel");
        String text = (String) event.get("text");

        // Find or register user
        BotUser user = botService.findBotUser(BotConfiguration.BotPlatform.SLACK, userId)
                .orElseGet(() -> botService.registerUser(
                        BotConfiguration.BotPlatform.SLACK,
                        userId,
                        userId,
                        null,
                        null
                ));

        // Log message
        botService.logInboundMessage(user, config, text, BotMessage.MessageType.TEXT);

        // If DM, respond
        if (channelId.startsWith("D")) {
            if (!user.getVerified()) {
                sendMessage(userId, channelId,
                        "Please link your LMS account first. Use `/lms link` to get started.", null);
            }
        }
    }

    private void handleMention(Map<String, Object> event, BotConfiguration config) {
        String userId = (String) event.get("user");
        String channelId = (String) event.get("channel");
        String text = (String) event.get("text");

        sendMessage(userId, channelId,
                "👋 Hi! I'm the FREE LMS Bot. Use `/lms help` to see what I can do!", null);
    }

    private void handleAppHomeOpened(Map<String, Object> event, BotConfiguration config) {
        String userId = (String) event.get("user");

        // Publish app home view
        // In real implementation, use views.publish API
        log.info("App home opened by user {}", userId);
    }

    private void handleAction(String userId, String actionId, String value, BotConfiguration config) {
        log.info("Action {} with value {} from user {}", actionId, value, userId);

        botService.findBotUser(BotConfiguration.BotPlatform.SLACK, userId)
                .ifPresent(user -> {
                    switch (actionId) {
                        case "link_account" -> {
                            String code = botService.generateVerificationCode(user.getId());
                            sendMessage(userId, null,
                                    String.format("Your verification code is: `%s`\n" +
                                            "Enter this in your LMS profile to complete linking.", code), null);
                        }
                        case "toggle_notifications" -> {
                            boolean newState = !user.getNotificationsEnabled();
                            botService.updateUserPreferences(user.getId(),
                                    Map.of("notificationsEnabled", newState));
                            sendMessage(userId, null,
                                    newState ? "🔔 Notifications enabled!" : "🔕 Notifications disabled.", null);
                        }
                    }
                });
    }

    private String handleLmsCommand(BotUser user, String text) {
        if (text == null || text.isEmpty() || text.equals("help")) {
            return """
                    *FREE LMS Bot Commands*

                    `/lms link` - Link your LMS account
                    `/lms courses` - View your courses
                    `/lms grades` - Check recent grades
                    `/lms settings` - Notification settings
                    `/lms help` - Show this help
                    """;
        }

        return switch (text.split("\\s+")[0]) {
            case "link" -> {
                String code = botService.generateVerificationCode(user.getId());
                yield String.format("Your verification code is: `%s`\n" +
                        "Enter this in your LMS profile to complete linking.", code);
            }
            case "courses" -> handleCoursesSlashCommand(user);
            case "grades" -> handleGradesSlashCommand(user);
            case "settings" -> handleSettingsSlashCommand(user);
            default -> "Unknown subcommand. Try `/lms help`.";
        };
    }

    private String handleCoursesSlashCommand(BotUser user) {
        if (!user.getVerified()) {
            return "Please link your account first with `/lms link`";
        }
        return "📚 Fetching your courses...";
    }

    private String handleGradesSlashCommand(BotUser user) {
        if (!user.getVerified()) {
            return "Please link your account first with `/lms link`";
        }
        return "📊 Fetching your grades...";
    }

    private String handleSettingsSlashCommand(BotUser user) {
        if (!user.getVerified()) {
            return "Please link your account first with `/lms link`";
        }
        return String.format("""
                *Notification Settings*
                • All notifications: %s
                • Course updates: %s
                • Assignment reminders: %s
                • Grade notifications: %s
                """,
                user.getNotificationsEnabled() ? "✅" : "❌",
                user.getCourseUpdates() ? "✅" : "❌",
                user.getAssignmentReminders() ? "✅" : "❌",
                user.getGradeNotifications() ? "✅" : "❌"
        );
    }
}
