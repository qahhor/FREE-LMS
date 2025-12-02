package com.freelms.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for RestTemplate HTTP client with configurable timeouts.
 * Timeouts can be configured via environment variables:
 * - HTTP_CONNECT_TIMEOUT: Connection timeout in milliseconds (default: 10000)
 * - HTTP_READ_TIMEOUT: Read timeout in milliseconds (default: 30000)
 */
@Configuration
public class RestTemplateConfig {

    @Value("${http.client.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${http.client.read-timeout:30000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }
}
