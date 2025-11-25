package com.freelms.mentoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.freelms.mentoring", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class MentoringServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MentoringServiceApplication.class, args);
    }
}
