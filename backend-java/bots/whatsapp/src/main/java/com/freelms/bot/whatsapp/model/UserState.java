package com.freelms.bot.whatsapp.model;

import lombok.Data;

@Data
public class UserState {
    private String phoneNumber;
    private Language language = Language.EN;
    private String lastCommand;
    private boolean authenticated;
    private String accessToken;

    public UserState(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
