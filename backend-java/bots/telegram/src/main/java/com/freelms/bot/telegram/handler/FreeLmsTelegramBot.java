package com.freelms.bot.telegram.handler;

import com.freelms.bot.telegram.config.BotConfig;
import com.freelms.bot.telegram.model.Course;
import com.freelms.bot.telegram.model.Language;
import com.freelms.bot.telegram.model.Translations;
import com.freelms.bot.telegram.service.LmsApiService;
import com.freelms.bot.telegram.service.UserStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FreeLmsTelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserStateService userStateService;
    private final LmsApiService lmsApiService;

    public FreeLmsTelegramBot(BotConfig botConfig,
                               UserStateService userStateService,
                               LmsApiService lmsApiService) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.userStateService = userStateService;
        this.lmsApiService = lmsApiService;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update.getMessage());
            }
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }

    private void handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();
        Language lang = userStateService.getLanguage(chatId);

        log.info("Message from {}: {}", chatId, text);

        if (text.startsWith("/start")) {
            sendLanguageSelection(chatId);
        } else if (text.startsWith("/help")) {
            sendHelp(chatId, lang);
        } else if (text.startsWith("/courses") || matchesPattern(text, "browse_courses", lang)) {
            sendCourseList(chatId, lang);
        } else if (text.startsWith("/mycourses") || matchesPattern(text, "my_courses", lang)) {
            sendUserCourses(chatId, lang);
        } else if (matchesPattern(text, "language", lang)) {
            sendLanguageSelection(chatId);
        } else if (matchesPattern(text, "help", lang)) {
            sendHelp(chatId, lang);
        } else if (matchesPattern(text, "profile", lang)) {
            sendProfile(chatId, lang);
        } else {
            // Default response
            sendMainMenu(chatId, lang);
        }
    }

    private boolean matchesPattern(String text, String key, Language lang) {
        String pattern = Translations.get(lang, key);
        return text.equalsIgnoreCase(pattern) || text.toLowerCase().contains(pattern.toLowerCase().substring(2));
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        log.info("Callback from {}: {}", chatId, data);

        if (data.startsWith("lang_")) {
            String langCode = data.substring(5);
            Language language = Language.fromCode(langCode);
            userStateService.setLanguage(chatId, language);

            // Edit message to confirm language selection
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(chatId.toString());
            editMessage.setMessageId(messageId);
            editMessage.setText(Translations.get(language, "language_set") + language.getName());

            try {
                execute(editMessage);
            } catch (TelegramApiException e) {
                log.error("Error editing message", e);
            }

            // Send main menu
            sendMainMenu(chatId, language);
        }
    }

    private void sendLanguageSelection(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(Translations.get(Language.EN, "welcome"));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Row 1: English, Russian
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton(Language.EN.getDisplayName(), "lang_en"));
        row1.add(createInlineButton(Language.RU.getDisplayName(), "lang_ru"));
        rows.add(row1);

        // Row 2: Uzbek, Arabic
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton(Language.UZ.getDisplayName(), "lang_uz"));
        row2.add(createInlineButton(Language.AR.getDisplayName(), "lang_ar"));
        rows.add(row2);

        // Row 3: Kazakh, Turkish
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton(Language.KK.getDisplayName(), "lang_kk"));
        row3.add(createInlineButton(Language.TR.getDisplayName(), "lang_tr"));
        rows.add(row3);

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        sendMessage(message);
    }

    private void sendMainMenu(Long chatId, Language lang) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(Translations.get(lang, "main_menu"));

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        // Row 1
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(Translations.get(lang, "my_courses")));
        row1.add(new KeyboardButton(Translations.get(lang, "browse_courses")));
        keyboard.add(row1);

        // Row 2
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(Translations.get(lang, "profile")));
        row2.add(new KeyboardButton(Translations.get(lang, "help")));
        keyboard.add(row2);

        // Row 3
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton(Translations.get(lang, "language")));
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        sendMessage(message);
    }

    private void sendCourseList(Long chatId, Language lang) {
        List<Course> courses = lmsApiService.getCourses(1, 5);

        StringBuilder sb = new StringBuilder();
        sb.append(Translations.get(lang, "courses_header"));

        if (courses.isEmpty()) {
            sb.append(Translations.get(lang, "no_courses"));
        } else {
            int index = 1;
            for (Course course : courses) {
                sb.append(index++).append(". ").append(course.getTitle()).append("\n");
                sb.append("   ").append(Translations.get(lang, "level")).append(": ").append(course.getLevel()).append("\n");
                if (course.isFree()) {
                    sb.append("   ").append(Translations.get(lang, "free")).append("\n\n");
                } else {
                    sb.append("   ").append(Translations.get(lang, "price")).append(course.getPrice()).append("\n\n");
                }
            }
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(sb.toString());
        sendMessage(message);
    }

    private void sendUserCourses(Long chatId, Language lang) {
        List<Course> courses = lmsApiService.getUserCourses(null);

        StringBuilder sb = new StringBuilder();
        sb.append(Translations.get(lang, "my_courses_header"));

        if (courses.isEmpty()) {
            sb.append(Translations.get(lang, "no_courses"));
        } else {
            int index = 1;
            for (Course course : courses) {
                sb.append(index++).append(". ").append(course.getTitle()).append("\n");
                sb.append("   ").append(Translations.get(lang, "level")).append(": ").append(course.getLevel()).append("\n\n");
            }
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(sb.toString());
        sendMessage(message);
    }

    private void sendHelp(Long chatId, Language lang) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(Translations.get(lang, "help_text"));
        sendMessage(message);
    }

    private void sendProfile(Long chatId, Language lang) {
        // TODO: Implement profile with real user data
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("\uD83D\uDC64 Profile\n\nTo view your profile, please login via the web interface:\nhttps://your-lms-domain.com");
        sendMessage(message);
    }

    private InlineKeyboardButton createInlineButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }
}
