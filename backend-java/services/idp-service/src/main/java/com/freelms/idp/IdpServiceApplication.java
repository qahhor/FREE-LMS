package com.freelms.idp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.freelms.idp", "com.freelms.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class IdpServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdpServiceApplication.class, args);
    }
}
