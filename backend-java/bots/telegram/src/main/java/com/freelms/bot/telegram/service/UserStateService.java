package com.freelms.bot.telegram.service;

import com.freelms.bot.telegram.model.Language;
import com.freelms.bot.telegram.model.UserState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    public UserState getOrCreate(Long chatId) {
        return userStates.computeIfAbsent(chatId, UserState::new);
    }

    public Language getLanguage(Long chatId) {
        return getOrCreate(chatId).getLanguage();
    }

    public void setLanguage(Long chatId, Language language) {
        getOrCreate(chatId).setLanguage(language);
    }

    public void setLastCommand(Long chatId, String command) {
        getOrCreate(chatId).setLastCommand(command);
    }

    public String getLastCommand(Long chatId) {
        return getOrCreate(chatId).getLastCommand();
    }
}
