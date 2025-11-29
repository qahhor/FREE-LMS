package com.freelms.bot.telegram.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class BotConfig {

    private String token;
    private String username;
    private String apiUrl = "http://localhost:8080/api/v1";
}
