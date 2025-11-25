package com.freelms.skills;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.freelms.skills", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class SkillsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkillsServiceApplication.class, args);
    }
}
