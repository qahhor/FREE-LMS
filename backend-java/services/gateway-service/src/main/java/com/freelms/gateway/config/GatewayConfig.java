package com.freelms.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**", "/api/v1/users/**")
                        .uri("lb://auth-service"))

                // Course Service
                .route("course-service", r -> r
                        .path("/api/v1/courses/**", "/api/v1/categories/**",
                              "/api/v1/lessons/**", "/api/v1/modules/**",
                              "/api/v1/quizzes/**", "/api/v1/videos/**")
                        .uri("lb://course-service"))

                // Enrollment Service
                .route("enrollment-service", r -> r
                        .path("/api/v1/enrollments/**", "/api/v1/certificates/**",
                              "/api/v1/progress/**", "/api/v1/gamification/**")
                        .uri("lb://enrollment-service"))

                // Payment Service
                .route("payment-service", r -> r
                        .path("/api/v1/payments/**", "/api/v1/subscriptions/**",
                              "/api/v1/webhooks/**", "/api/v1/orders/**")
                        .uri("lb://payment-service"))

                // Notification Service
                .route("notification-service", r -> r
                        .path("/api/v1/notifications/**", "/api/v1/push/**")
                        .uri("lb://notification-service"))

                // Analytics Service
                .route("analytics-service", r -> r
                        .path("/api/v1/analytics/**", "/api/v1/recommendations/**",
                              "/api/v1/search/**")
                        .uri("lb://analytics-service"))

                // Organization Service
                .route("organization-service", r -> r
                        .path("/api/v1/organizations/**", "/api/v1/scorm/**",
                              "/api/v1/webinars/**", "/api/v1/api-keys/**",
                              "/api/v1/sso/**")
                        .uri("lb://organization-service"))

                .build();
    }
}
