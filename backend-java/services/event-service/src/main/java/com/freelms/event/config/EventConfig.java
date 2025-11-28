package com.freelms.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for Event Service.
 */
@Configuration
public class EventConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
