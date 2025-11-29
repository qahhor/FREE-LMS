package com.freelms.bot.telegram.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
    EN("en", "English", "\uD83C\uDDEC\uD83C\uDDE7"),
    RU("ru", "Русский", "\uD83C\uDDF7\uD83C\uDDFA"),
    UZ("uz", "O'zbekcha", "\uD83C\uDDFA\uD83C\uDDFF"),
    AR("ar", "العربية", "\uD83C\uDDF8\uD83C\uDDE6"),
    KK("kk", "Қазақша", "\uD83C\uDDF0\uD83C\uDDFF"),
    TR("tr", "Türkçe", "\uD83C\uDDF9\uD83C\uDDF7");

    private final String code;
    private final String name;
    private final String flag;

    public static Language fromCode(String code) {
        for (Language lang : values()) {
            if (lang.code.equals(code)) {
                return lang;
            }
        }
        return EN;
    }

    public String getDisplayName() {
        return flag + " " + name;
    }
}
