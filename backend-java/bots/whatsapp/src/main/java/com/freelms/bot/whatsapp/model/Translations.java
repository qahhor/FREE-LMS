package com.freelms.bot.whatsapp.model;

import java.util.EnumMap;
import java.util.Map;

public class Translations {

    private static final Map<Language, Map<String, String>> translations = new EnumMap<>(Language.class);

    static {
        // English translations
        Map<String, String> en = Map.ofEntries(
                Map.entry("welcome", "🎓 *Welcome to FREE LMS Bot!*\n\nPlease select your language:"),
                Map.entry("language_set", "✅ Language set to: "),
                Map.entry("main_menu", "📚 *Main Menu*\n\nSelect an option:"),
                Map.entry("my_courses", "📖 My Courses"),
                Map.entry("browse_courses", "🔍 Browse Courses"),
                Map.entry("profile", "👤 Profile"),
                Map.entry("help", "❓ Help"),
                Map.entry("language", "🌐 Language"),
                Map.entry("courses_header", "📚 *Available Courses*\n\n"),
                Map.entry("my_courses_header", "📖 *My Courses*\n\n"),
                Map.entry("no_courses", "No courses available at the moment."),
                Map.entry("level", "Level"),
                Map.entry("price", "💰 Price: $"),
                Map.entry("free", "✅ FREE"),
                Map.entry("help_text", "❓ *Help*\n\n*Commands:*\n• *courses* - Browse available courses\n• *mycourses* - View your enrolled courses\n• *profile* - View your profile\n• *language* - Change language\n• *help* - Show this help message\n\nFor support, contact: support@freelms.com"),
                Map.entry("menu_hint", "Reply with a number or keyword:")
        );
        translations.put(Language.EN, en);

        // Russian translations
        Map<String, String> ru = Map.ofEntries(
                Map.entry("welcome", "🎓 *Добро пожаловать в FREE LMS Bot!*\n\nВыберите язык:"),
                Map.entry("language_set", "✅ Язык установлен: "),
                Map.entry("main_menu", "📚 *Главное меню*\n\nВыберите опцию:"),
                Map.entry("my_courses", "📖 Мои курсы"),
                Map.entry("browse_courses", "🔍 Обзор курсов"),
                Map.entry("profile", "👤 Профиль"),
                Map.entry("help", "❓ Помощь"),
                Map.entry("language", "🌐 Язык"),
                Map.entry("courses_header", "📚 *Доступные курсы*\n\n"),
                Map.entry("my_courses_header", "📖 *Мои курсы*\n\n"),
                Map.entry("no_courses", "На данный момент курсов нет."),
                Map.entry("level", "Уровень"),
                Map.entry("price", "💰 Цена: $"),
                Map.entry("free", "✅ БЕСПЛАТНО"),
                Map.entry("help_text", "❓ *Помощь*\n\n*Команды:*\n• *courses* - Просмотр доступных курсов\n• *mycourses* - Ваши курсы\n• *profile* - Ваш профиль\n• *language* - Сменить язык\n• *help* - Показать эту справку\n\nПоддержка: support@freelms.com"),
                Map.entry("menu_hint", "Ответьте номером или ключевым словом:")
        );
        translations.put(Language.RU, ru);

        // Uzbek translations
        Map<String, String> uz = Map.ofEntries(
                Map.entry("welcome", "🎓 *FREE LMS Botga xush kelibsiz!*\n\nTilni tanlang:"),
                Map.entry("language_set", "✅ Til o'rnatildi: "),
                Map.entry("main_menu", "📚 *Asosiy menyu*\n\nTanlang:"),
                Map.entry("my_courses", "📖 Mening kurslarim"),
                Map.entry("browse_courses", "🔍 Kurslarni ko'rish"),
                Map.entry("profile", "👤 Profil"),
                Map.entry("help", "❓ Yordam"),
                Map.entry("language", "🌐 Til"),
                Map.entry("courses_header", "📚 *Mavjud kurslar*\n\n"),
                Map.entry("my_courses_header", "📖 *Mening kurslarim*\n\n"),
                Map.entry("no_courses", "Hozircha kurslar mavjud emas."),
                Map.entry("level", "Daraja"),
                Map.entry("price", "💰 Narx: $"),
                Map.entry("free", "✅ BEPUL"),
                Map.entry("help_text", "❓ *Yordam*\n\n*Buyruqlar:*\n• *courses* - Mavjud kurslar\n• *mycourses* - Sizning kurslaringiz\n• *profile* - Profilingiz\n• *language* - Tilni o'zgartirish\n• *help* - Yordam\n\nQo'llab-quvvatlash: support@freelms.com"),
                Map.entry("menu_hint", "Raqam yoki kalit so'z bilan javob bering:")
        );
        translations.put(Language.UZ, uz);

        // Arabic translations
        Map<String, String> ar = Map.ofEntries(
                Map.entry("welcome", "🎓 *مرحبًا بك في FREE LMS Bot!*\n\nاختر لغتك:"),
                Map.entry("language_set", "✅ تم تعيين اللغة: "),
                Map.entry("main_menu", "📚 *القائمة الرئيسية*\n\nاختر خيارًا:"),
                Map.entry("my_courses", "📖 دوراتي"),
                Map.entry("browse_courses", "🔍 تصفح الدورات"),
                Map.entry("profile", "👤 الملف الشخصي"),
                Map.entry("help", "❓ مساعدة"),
                Map.entry("language", "🌐 اللغة"),
                Map.entry("courses_header", "📚 *الدورات المتاحة*\n\n"),
                Map.entry("my_courses_header", "📖 *دوراتي*\n\n"),
                Map.entry("no_courses", "لا توجد دورات متاحة حاليًا."),
                Map.entry("level", "المستوى"),
                Map.entry("price", "💰 السعر: $"),
                Map.entry("free", "✅ مجاني"),
                Map.entry("help_text", "❓ *مساعدة*\n\n*الأوامر:*\n• *courses* - تصفح الدورات\n• *mycourses* - دوراتك\n• *profile* - ملفك الشخصي\n• *language* - تغيير اللغة\n• *help* - عرض المساعدة\n\nالدعم: support@freelms.com"),
                Map.entry("menu_hint", "أجب برقم أو كلمة مفتاحية:")
        );
        translations.put(Language.AR, ar);

        // Kazakh translations
        Map<String, String> kk = Map.ofEntries(
                Map.entry("welcome", "🎓 *FREE LMS Bot-қа қош келдіңіз!*\n\nТілді таңдаңыз:"),
                Map.entry("language_set", "✅ Тіл орнатылды: "),
                Map.entry("main_menu", "📚 *Басты мәзір*\n\nТаңдаңыз:"),
                Map.entry("my_courses", "📖 Менің курстарым"),
                Map.entry("browse_courses", "🔍 Курстарды қарау"),
                Map.entry("profile", "👤 Профиль"),
                Map.entry("help", "❓ Көмек"),
                Map.entry("language", "🌐 Тіл"),
                Map.entry("courses_header", "📚 *Қол жетімді курстар*\n\n"),
                Map.entry("my_courses_header", "📖 *Менің курстарым*\n\n"),
                Map.entry("no_courses", "Қазір курстар жоқ."),
                Map.entry("level", "Деңгей"),
                Map.entry("price", "💰 Бағасы: $"),
                Map.entry("free", "✅ ТЕГІН"),
                Map.entry("help_text", "❓ *Көмек*\n\n*Командалар:*\n• *courses* - Курстар\n• *mycourses* - Сіздің курстарыңыз\n• *profile* - Профиль\n• *language* - Тілді өзгерту\n• *help* - Көмек\n\nҚолдау: support@freelms.com"),
                Map.entry("menu_hint", "Нөмір немесе кілт сөзбен жауап беріңіз:")
        );
        translations.put(Language.KK, kk);

        // Turkish translations
        Map<String, String> tr = Map.ofEntries(
                Map.entry("welcome", "🎓 *FREE LMS Bot'a hoş geldiniz!*\n\nDilinizi seçin:"),
                Map.entry("language_set", "✅ Dil ayarlandı: "),
                Map.entry("main_menu", "📚 *Ana Menü*\n\nBir seçenek seçin:"),
                Map.entry("my_courses", "📖 Kurslarım"),
                Map.entry("browse_courses", "🔍 Kurslara Göz At"),
                Map.entry("profile", "👤 Profil"),
                Map.entry("help", "❓ Yardım"),
                Map.entry("language", "🌐 Dil"),
                Map.entry("courses_header", "📚 *Mevcut Kurslar*\n\n"),
                Map.entry("my_courses_header", "📖 *Kurslarım*\n\n"),
                Map.entry("no_courses", "Şu anda mevcut kurs yok."),
                Map.entry("level", "Seviye"),
                Map.entry("price", "💰 Fiyat: $"),
                Map.entry("free", "✅ ÜCRETSİZ"),
                Map.entry("help_text", "❓ *Yardım*\n\n*Komutlar:*\n• *courses* - Kursları görüntüle\n• *mycourses* - Kurslarınız\n• *profile* - Profiliniz\n• *language* - Dili değiştir\n• *help* - Yardım\n\nDestek: support@freelms.com"),
                Map.entry("menu_hint", "Numara veya anahtar kelime ile yanıtlayın:")
        );
        translations.put(Language.TR, tr);
    }

    public static String get(Language lang, String key) {
        Map<String, String> langMap = translations.getOrDefault(lang, translations.get(Language.EN));
        return langMap.getOrDefault(key, translations.get(Language.EN).getOrDefault(key, key));
    }
}
