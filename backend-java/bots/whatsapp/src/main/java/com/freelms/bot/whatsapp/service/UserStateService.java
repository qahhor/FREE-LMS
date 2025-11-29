package com.freelms.bot.whatsapp.service;

import com.freelms.bot.whatsapp.model.Language;
import com.freelms.bot.whatsapp.model.UserState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {

    private final Map<String, UserState> userStates = new ConcurrentHashMap<>();

    public UserState getOrCreate(String phoneNumber) {
        return userStates.computeIfAbsent(phoneNumber, UserState::new);
    }

    public Language getLanguage(String phoneNumber) {
        return getOrCreate(phoneNumber).getLanguage();
    }

    public void setLanguage(String phoneNumber, Language language) {
        getOrCreate(phoneNumber).setLanguage(language);
    }

    public void setLastCommand(String phoneNumber, String command) {
        getOrCreate(phoneNumber).setLastCommand(command);
    }

    public String getLastCommand(String phoneNumber) {
        return getOrCreate(phoneNumber).getLastCommand();
    }
}
