package com.freelms.bot.whatsapp.model;

import lombok.Getter;

@Getter
public enum Language {
    EN("en", "English", "🇺🇸"),
    RU("ru", "Русский", "🇷🇺"),
    UZ("uz", "O'zbek", "🇺🇿"),
    AR("ar", "العربية", "🇸🇦"),
    KK("kk", "Қазақ", "🇰🇿"),
    TR("tr", "Türkçe", "🇹🇷");

    private final String code;
    private final String name;
    private final String flag;

    Language(String code, String name, String flag) {
        this.code = code;
        this.name = name;
        this.flag = flag;
    }

    public String getDisplayName() {
        return flag + " " + name;
    }

    public static Language fromCode(String code) {
        for (Language lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return EN;
    }
}
