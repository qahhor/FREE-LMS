package com.freelms.bot.whatsapp.handler;

import com.freelms.bot.whatsapp.model.Course;
import com.freelms.bot.whatsapp.model.Language;
import com.freelms.bot.whatsapp.model.Translations;
import com.freelms.bot.whatsapp.service.LmsApiService;
import com.freelms.bot.whatsapp.service.UserStateService;
import com.freelms.bot.whatsapp.service.WhatsAppMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final UserStateService userStateService;
    private final LmsApiService lmsApiService;
    private final WhatsAppMessageService messageService;

    public void handleMessage(String from, String body) {
        log.info("Message from {}: {}", from, body);

        Language lang = userStateService.getLanguage(from);
        String text = body.trim().toLowerCase();

        if (text.equals("start") || text.equals("hi") || text.equals("hello") || text.equals("menu")) {
            sendLanguageSelection(from);
        } else if (text.startsWith("lang_")) {
            handleLanguageSelection(from, text);
        } else if (text.equals("1") || text.equals("courses") || matchesPattern(text, "browse_courses", lang)) {
            sendCourseList(from, lang);
        } else if (text.equals("2") || text.equals("mycourses") || matchesPattern(text, "my_courses", lang)) {
            sendUserCourses(from, lang);
        } else if (text.equals("3") || text.equals("profile") || matchesPattern(text, "profile", lang)) {
            sendProfile(from, lang);
        } else if (text.equals("4") || text.equals("help") || matchesPattern(text, "help", lang)) {
            sendHelp(from, lang);
        } else if (text.equals("5") || text.equals("language") || matchesPattern(text, "language", lang)) {
            sendLanguageSelection(from);
        } else {
            sendMainMenu(from, lang);
        }
    }

    private boolean matchesPattern(String text, String key, Language lang) {
        String pattern = Translations.get(lang, key).toLowerCase();
        return text.contains(pattern.substring(2)); // Skip emoji prefix
    }

    private void handleLanguageSelection(String from, String text) {
        String langCode = text.replace("lang_", "");
        Language language = Language.fromCode(langCode);
        userStateService.setLanguage(from, language);

        String response = Translations.get(language, "language_set") + language.getName();
        messageService.sendMessage(from, response);

        sendMainMenu(from, language);
    }

    private void sendLanguageSelection(String from) {
        StringBuilder sb = new StringBuilder();
        sb.append(Translations.get(Language.EN, "welcome"));
        sb.append("\n\n");

        sb.append("Reply with:\n");
        sb.append("*lang_en* - ").append(Language.EN.getDisplayName()).append("\n");
        sb.append("*lang_ru* - ").append(Language.RU.getDisplayName()).append("\n");
        sb.append("*lang_uz* - ").append(Language.UZ.getDisplayName()).append("\n");
        sb.append("*lang_ar* - ").append(Language.AR.getDisplayName()).append("\n");
        sb.append("*lang_kk* - ").append(Language.KK.getDisplayName()).append("\n");
        sb.append("*lang_tr* - ").append(Language.TR.getDisplayName()).append("\n");

        messageService.sendMessage(from, sb.toString());
    }

    private void sendMainMenu(String from, Language lang) {
        StringBuilder sb = new StringBuilder();
        sb.append(Translations.get(lang, "main_menu"));
        sb.append("\n\n");

        sb.append("*1.* ").append(Translations.get(lang, "browse_courses")).append("\n");
        sb.append("*2.* ").append(Translations.get(lang, "my_courses")).append("\n");
        sb.append("*3.* ").append(Translations.get(lang, "profile")).append("\n");
        sb.append("*4.* ").append(Translations.get(lang, "help")).append("\n");
        sb.append("*5.* ").append(Translations.get(lang, "language")).append("\n");
        sb.append("\n").append(Translations.get(lang, "menu_hint"));

        messageService.sendMessage(from, sb.toString());
    }

    private void sendCourseList(String from, Language lang) {
        List<Course> courses = lmsApiService.getCourses(1, 5);

        StringBuilder sb = new StringBuilder();
        sb.append(Translations.get(lang, "courses_header"));

        if (courses.isEmpty()) {
            sb.append(Translations.get(lang, "no_courses"));
        } else {
            int index = 1;
            for (Course course : courses) {
                sb.append(index++).append(". *").append(course.getTitle()).append("*\n");
                sb.append("   ").append(Translations.get(lang, "level")).append(": ").append(course.getLevel()).append("\n");
                if (course.isFree()) {
                    sb.append("   ").append(Translations.get(lang, "free")).append("\n\n");
                } else {
                    sb.append("   ").append(Translations.get(lang, "price")).append(course.getPrice()).append("\n\n");
                }
            }
        }

        sb.append("\nReply *menu* to go back.");

        messageService.sendMessage(from, sb.toString());
    }

    private void sendUserCourses(String from, Language lang) {
        List<Course> courses = lmsApiService.getUserCourses(from);

        StringBuilder sb = new StringBuilder();
        sb.append(Translations.get(lang, "my_courses_header"));

        if (courses.isEmpty()) {
            sb.append(Translations.get(lang, "no_courses"));
        } else {
            int index = 1;
            for (Course course : courses) {
                sb.append(index++).append(". *").append(course.getTitle()).append("*\n");
                sb.append("   ").append(Translations.get(lang, "level")).append(": ").append(course.getLevel()).append("\n\n");
            }
        }

        sb.append("\nReply *menu* to go back.");

        messageService.sendMessage(from, sb.toString());
    }

    private void sendHelp(String from, Language lang) {
        messageService.sendMessage(from, Translations.get(lang, "help_text"));
    }

    private void sendProfile(String from, Language lang) {
        String message = "👤 *Profile*\n\n" +
                "To view your profile, please login via the web interface:\n" +
                "https://your-lms-domain.com\n\n" +
                "Reply *menu* to go back.";
        messageService.sendMessage(from, message);
    }
}
