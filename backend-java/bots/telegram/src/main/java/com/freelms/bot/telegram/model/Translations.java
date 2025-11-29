package com.freelms.bot.telegram.model;

import java.util.HashMap;
import java.util.Map;

public class Translations {

    private static final Map<Language, Map<String, String>> translations = new HashMap<>();

    static {
        // English
        Map<String, String> en = new HashMap<>();
        en.put("welcome", "Welcome to FREE LMS! \uD83C\uDF93\n\nChoose a language to continue:");
        en.put("main_menu", "Main Menu");
        en.put("my_courses", "\uD83D\uDCDA My Courses");
        en.put("browse_courses", "\uD83D\uDD0D Browse Courses");
        en.put("profile", "\uD83D\uDC64 Profile");
        en.put("help", "\u2753 Help");
        en.put("language", "\uD83C\uDF10 Change Language");
        en.put("language_set", "✅ Language set to: ");
        en.put("no_courses", "\uD83D\uDCED No courses available at the moment.");
        en.put("error", "❌ An error occurred. Please try again later.");
        en.put("courses_header", "\uD83D\uDCD6 Available Courses:\n\n");
        en.put("my_courses_header", "\uD83D\uDCDA Your Enrolled Courses:\n\n");
        en.put("level", "Level");
        en.put("free", "\uD83C\uDD93 Free");
        en.put("price", "\uD83D\uDCB0 $");
        en.put("help_text", """
            \uD83E\uDD16 FREE LMS Bot Help

            Available commands:
            /start - Start the bot and select language
            /help - Show this help message
            /courses - Browse available courses
            /mycourses - View your enrolled courses

            You can also use the menu buttons!
            """);
        translations.put(Language.EN, en);

        // Russian
        Map<String, String> ru = new HashMap<>();
        ru.put("welcome", "Добро пожаловать в FREE LMS! \uD83C\uDF93\n\nВыберите язык:");
        ru.put("main_menu", "Главное меню");
        ru.put("my_courses", "\uD83D\uDCDA Мои курсы");
        ru.put("browse_courses", "\uD83D\uDD0D Обзор курсов");
        ru.put("profile", "\uD83D\uDC64 Профиль");
        ru.put("help", "\u2753 Помощь");
        ru.put("language", "\uD83C\uDF10 Изменить язык");
        ru.put("language_set", "✅ Язык установлен: ");
        ru.put("no_courses", "\uD83D\uDCED Курсы пока недоступны.");
        ru.put("error", "❌ Произошла ошибка. Попробуйте позже.");
        ru.put("courses_header", "\uD83D\uDCD6 Доступные курсы:\n\n");
        ru.put("my_courses_header", "\uD83D\uDCDA Ваши курсы:\n\n");
        ru.put("level", "Уровень");
        ru.put("free", "\uD83C\uDD93 Бесплатно");
        ru.put("price", "\uD83D\uDCB0 $");
        ru.put("help_text", """
            \uD83E\uDD16 Справка FREE LMS Bot

            Доступные команды:
            /start - Запустить бота и выбрать язык
            /help - Показать справку
            /courses - Просмотр курсов
            /mycourses - Мои курсы

            Также используйте кнопки меню!
            """);
        translations.put(Language.RU, ru);

        // Uzbek
        Map<String, String> uz = new HashMap<>();
        uz.put("welcome", "FREE LMS ga xush kelibsiz! \uD83C\uDF93\n\nTilni tanlang:");
        uz.put("main_menu", "Asosiy menyu");
        uz.put("my_courses", "\uD83D\uDCDA Mening kurslarim");
        uz.put("browse_courses", "\uD83D\uDD0D Kurslarni ko'rish");
        uz.put("profile", "\uD83D\uDC64 Profil");
        uz.put("help", "\u2753 Yordam");
        uz.put("language", "\uD83C\uDF10 Tilni o'zgartirish");
        uz.put("language_set", "✅ Til o'rnatildi: ");
        uz.put("no_courses", "\uD83D\uDCED Hozircha kurslar mavjud emas.");
        uz.put("error", "❌ Xatolik yuz berdi. Keyinroq urinib ko'ring.");
        uz.put("courses_header", "\uD83D\uDCD6 Mavjud kurslar:\n\n");
        uz.put("my_courses_header", "\uD83D\uDCDA Sizning kurslaringiz:\n\n");
        uz.put("level", "Daraja");
        uz.put("free", "\uD83C\uDD93 Bepul");
        uz.put("price", "\uD83D\uDCB0 $");
        uz.put("help_text", """
            \uD83E\uDD16 FREE LMS Bot Yordam

            Mavjud buyruqlar:
            /start - Botni ishga tushirish va til tanlash
            /help - Yordam ko'rsatish
            /courses - Kurslarni ko'rish
            /mycourses - Mening kurslarim

            Menyu tugmalaridan ham foydalaning!
            """);
        translations.put(Language.UZ, uz);

        // Arabic
        Map<String, String> ar = new HashMap<>();
        ar.put("welcome", "مرحباً بك في FREE LMS! \uD83C\uDF93\n\nاختر اللغة:");
        ar.put("main_menu", "القائمة الرئيسية");
        ar.put("my_courses", "\uD83D\uDCDA دوراتي");
        ar.put("browse_courses", "\uD83D\uDD0D تصفح الدورات");
        ar.put("profile", "\uD83D\uDC64 الملف الشخصي");
        ar.put("help", "\u2753 مساعدة");
        ar.put("language", "\uD83C\uDF10 تغيير اللغة");
        ar.put("language_set", "✅ تم تعيين اللغة: ");
        ar.put("no_courses", "\uD83D\uDCED لا توجد دورات متاحة حالياً.");
        ar.put("error", "❌ حدث خطأ. حاول مرة أخرى لاحقاً.");
        ar.put("courses_header", "\uD83D\uDCD6 الدورات المتاحة:\n\n");
        ar.put("my_courses_header", "\uD83D\uDCDA دوراتك المسجلة:\n\n");
        ar.put("level", "المستوى");
        ar.put("free", "\uD83C\uDD93 مجاني");
        ar.put("price", "\uD83D\uDCB0 $");
        ar.put("help_text", """
            \uD83E\uDD16 مساعدة FREE LMS Bot

            الأوامر المتاحة:
            /start - بدء البوت واختيار اللغة
            /help - عرض المساعدة
            /courses - تصفح الدورات
            /mycourses - دوراتي

            يمكنك أيضاً استخدام أزرار القائمة!
            """);
        translations.put(Language.AR, ar);

        // Kazakh
        Map<String, String> kk = new HashMap<>();
        kk.put("welcome", "FREE LMS-ке қош келдіңіз! \uD83C\uDF93\n\nТілді таңдаңыз:");
        kk.put("main_menu", "Басты мәзір");
        kk.put("my_courses", "\uD83D\uDCDA Менің курстарым");
        kk.put("browse_courses", "\uD83D\uDD0D Курстарды қарау");
        kk.put("profile", "\uD83D\uDC64 Профиль");
        kk.put("help", "\u2753 Көмек");
        kk.put("language", "\uD83C\uDF10 Тілді өзгерту");
        kk.put("language_set", "✅ Тіл орнатылды: ");
        kk.put("no_courses", "\uD83D\uDCED Қазір курстар жоқ.");
        kk.put("error", "❌ Қате орын алды. Кейінірек қайталаңыз.");
        kk.put("courses_header", "\uD83D\uDCD6 Қол жетімді курстар:\n\n");
        kk.put("my_courses_header", "\uD83D\uDCDA Сіздің курстарыңыз:\n\n");
        kk.put("level", "Деңгей");
        kk.put("free", "\uD83C\uDD93 Тегін");
        kk.put("price", "\uD83D\uDCB0 $");
        kk.put("help_text", """
            \uD83E\uDD16 FREE LMS Bot Көмек

            Қол жетімді командалар:
            /start - Ботты бастау және тіл таңдау
            /help - Көмек көрсету
            /courses - Курстарды қарау
            /mycourses - Менің курстарым

            Мәзір батырмаларын да қолдануға болады!
            """);
        translations.put(Language.KK, kk);

        // Turkish
        Map<String, String> tr = new HashMap<>();
        tr.put("welcome", "FREE LMS'ye hoş geldiniz! \uD83C\uDF93\n\nDil seçin:");
        tr.put("main_menu", "Ana Menü");
        tr.put("my_courses", "\uD83D\uDCDA Kurslarım");
        tr.put("browse_courses", "\uD83D\uDD0D Kurslara Göz At");
        tr.put("profile", "\uD83D\uDC64 Profil");
        tr.put("help", "\u2753 Yardım");
        tr.put("language", "\uD83C\uDF10 Dili Değiştir");
        tr.put("language_set", "✅ Dil ayarlandı: ");
        tr.put("no_courses", "\uD83D\uDCED Şu anda mevcut kurs yok.");
        tr.put("error", "❌ Bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
        tr.put("courses_header", "\uD83D\uDCD6 Mevcut Kurslar:\n\n");
        tr.put("my_courses_header", "\uD83D\uDCDA Kayıtlı Kurslarınız:\n\n");
        tr.put("level", "Seviye");
        tr.put("free", "\uD83C\uDD93 Ücretsiz");
        tr.put("price", "\uD83D\uDCB0 $");
        tr.put("help_text", """
            \uD83E\uDD16 FREE LMS Bot Yardım

            Mevcut komutlar:
            /start - Botu başlat ve dil seç
            /help - Yardımı göster
            /courses - Kurslara göz at
            /mycourses - Kurslarım

            Menü düğmelerini de kullanabilirsiniz!
            """);
        translations.put(Language.TR, tr);
    }

    public static String get(Language language, String key) {
        Map<String, String> langTranslations = translations.getOrDefault(language, translations.get(Language.EN));
        return langTranslations.getOrDefault(key, translations.get(Language.EN).get(key));
    }
}
