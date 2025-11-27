package com.freelms.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bot Platform Service Application
 *
 * Micro-learning delivery through messengers:
 * - Telegram bots
 * - Slack apps
 * - WhatsApp Business API
 * - Microsoft Teams bots
 * - Discord bots
 *
 * Features:
 * - Micro-lessons and flashcards
 * - Daily challenges and quizzes
 * - Learning streaks and gamification
 * - Campaign management
 * - Analytics and engagement tracking
 */
@SpringBootApplication(scanBasePackages = {"com.freelms.bot", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
public class BotPlatformServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotPlatformServiceApplication.class, args);
    }
}
