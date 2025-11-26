package com.freelms.integration.security;

import com.freelms.integration.token.ApiTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Smartup LMS - Integration Service Security Configuration
 *
 * Configures API token authentication for external API access.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class IntegrationSecurityConfig {

    private final ApiTokenService apiTokenService;

    public IntegrationSecurityConfig(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/**")
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/certificates/verify/**").permitAll()

                // All other API endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(apiTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain healthSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/health", "/ready", "/metrics", "/swagger/**", "/v3/api-docs/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public ApiTokenAuthenticationFilter apiTokenAuthenticationFilter() {
        return new ApiTokenAuthenticationFilter(apiTokenService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Configure based on environment
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "X-API-Key",
                "Content-Type",
                "X-Request-ID",
                "X-Correlation-ID"
        ));
        configuration.setExposedHeaders(Arrays.asList(
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset"
        ));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
