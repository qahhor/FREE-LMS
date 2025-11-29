package com.freelms.bot.whatsapp.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "twilio")
public class TwilioConfig {

    private String accountSid;
    private String authToken;
    private String whatsappNumber;
    private String apiUrl = "http://localhost:8080/api/v1";

    @PostConstruct
    public void init() {
        if (accountSid != null && authToken != null) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized for WhatsApp bot");
        } else {
            log.warn("Twilio credentials not configured. Bot will run in demo mode.");
        }
    }
}
