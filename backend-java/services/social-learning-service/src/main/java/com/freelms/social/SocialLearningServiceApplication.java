package com.freelms.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.freelms.social", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class SocialLearningServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialLearningServiceApplication.class, args);
    }
}
