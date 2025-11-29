package com.freelms.bot.telegram.model;

import lombok.Data;

@Data
public class UserState {
    private Long chatId;
    private Language language = Language.EN;
    private String lastCommand;
    private boolean authenticated;
    private String accessToken;

    public UserState(Long chatId) {
        this.chatId = chatId;
    }
}
