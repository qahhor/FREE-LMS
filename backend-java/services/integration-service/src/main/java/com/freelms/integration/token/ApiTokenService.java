package com.freelms.integration.token;

import com.freelms.integration.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Smartup LMS - API Token Service
 *
 * Manages API tokens for external system integration.
 * Supports token creation, validation, rate limiting, and usage tracking.
 */
@Service
public class ApiTokenService {

    private static final Logger log = LoggerFactory.getLogger(ApiTokenService.class);
    private static final String TOKEN_PREFIX = "slms_";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final int TOKEN_LENGTH = 32;

    private final ApiTokenRepository tokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public ApiTokenService(ApiTokenRepository tokenRepository,
                          RedisTemplate<String, Object> redisTemplate) {
        this.tokenRepository = tokenRepository;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.secureRandom = new SecureRandom();
    }

    /**
     * Create a new API token
     */
    @Transactional
    public ApiTokenCreatedDto createToken(ApiTokenCreateDto dto, Long organizationId, Long userId) {
        // Generate secure token
        String rawToken = generateSecureToken();
        String tokenHash = passwordEncoder.encode(rawToken);
        String tokenKey = rawToken.substring(0, 16); // First 16 chars for lookup

        ApiToken token = ApiToken.builder()
                .tokenKey(tokenKey)
                .tokenHash(tokenHash)
                .name(dto.getName())
                .description(dto.getDescription())
                .organizationId(organizationId)
                .createdBy(userId)
                .type(dto.getType())
                .scopes(dto.getScopes())
                .expiresAt(dto.getExpiresAt())
                .rateLimit(dto.getRateLimit() != null ? dto.getRateLimit() : 1000)
                .allowedIps(dto.getAllowedIps() != null ? dto.getAllowedIps() : new HashSet<>())
                .build();

        token = tokenRepository.save(token);
        log.info("Created API token: {} for organization: {}", token.getName(), organizationId);

        return ApiTokenCreatedDto.builder()
                .id(token.getId())
                .token(rawToken) // Full token shown only once!
                .name(token.getName())
                .type(token.getType())
                .scopes(token.getScopes())
                .expiresAt(token.getExpiresAt())
                .message("Store this token securely. It will not be shown again.")
                .build();
    }

    /**
     * Validate token and return token details if valid
     */
    public Optional<ApiToken> validateToken(String rawToken, String clientIp) {
        if (rawToken == null || rawToken.length() < 16) {
            return Optional.empty();
        }

        String tokenKey = rawToken.substring(0, 16);
        Optional<ApiToken> tokenOpt = tokenRepository.findByTokenKey(tokenKey);

        if (tokenOpt.isEmpty()) {
            log.debug("Token not found: {}", tokenKey.substring(0, 8) + "...");
            return Optional.empty();
        }

        ApiToken token = tokenOpt.get();

        // Verify hash
        if (!passwordEncoder.matches(rawToken, token.getTokenHash())) {
            log.warn("Token hash mismatch for: {}", token.getName());
            return Optional.empty();
        }

        // Check if valid
        if (!token.isValid()) {
            log.debug("Token invalid or expired: {}", token.getName());
            return Optional.empty();
        }

        // Check IP whitelist
        if (!token.isIpAllowed(clientIp)) {
            log.warn("Token IP not allowed: {} from {}", token.getName(), clientIp);
            return Optional.empty();
        }

        // Check rate limit
        if (!checkRateLimit(token)) {
            log.warn("Token rate limit exceeded: {}", token.getName());
            return Optional.empty();
        }

        // Update usage stats
        updateUsageStats(token, clientIp);

        return Optional.of(token);
    }

    /**
     * Check if request is within rate limit
     */
    private boolean checkRateLimit(ApiToken token) {
        String rateLimitKey = RATE_LIMIT_PREFIX + token.getId();
        Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey);

        if (currentCount != null && currentCount == 1) {
            // First request in window - set expiry
            redisTemplate.expire(rateLimitKey, token.getRateLimitWindow(), TimeUnit.SECONDS);
        }

        return currentCount == null || currentCount <= token.getRateLimit();
    }

    /**
     * Update token usage statistics
     */
    @Transactional
    public void updateUsageStats(ApiToken token, String clientIp) {
        token.setLastUsedAt(Instant.now());
        token.setLastUsedIp(clientIp);
        token.setRequestCount(token.getRequestCount() + 1);
        tokenRepository.save(token);
    }

    /**
     * Get all tokens for organization
     */
    public List<ApiTokenResponseDto> getTokensForOrganization(Long organizationId) {
        return tokenRepository.findByOrganizationId(organizationId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get token by ID
     */
    public Optional<ApiTokenResponseDto> getToken(Long tokenId, Long organizationId) {
        return tokenRepository.findByIdAndOrganizationId(tokenId, organizationId)
                .map(this::toResponseDto);
    }

    /**
     * Update token
     */
    @Transactional
    public Optional<ApiTokenResponseDto> updateToken(Long tokenId, ApiTokenUpdateDto dto, Long organizationId) {
        Optional<ApiToken> tokenOpt = tokenRepository.findByIdAndOrganizationId(tokenId, organizationId);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        ApiToken token = tokenOpt.get();

        if (dto.getName() != null) token.setName(dto.getName());
        if (dto.getDescription() != null) token.setDescription(dto.getDescription());
        if (dto.getScopes() != null) token.setScopes(dto.getScopes());
        if (dto.getExpiresAt() != null) token.setExpiresAt(dto.getExpiresAt());
        if (dto.getRateLimit() != null) token.setRateLimit(dto.getRateLimit());
        if (dto.getAllowedIps() != null) token.setAllowedIps(dto.getAllowedIps());
        if (dto.getActive() != null) token.setActive(dto.getActive());

        token = tokenRepository.save(token);
        log.info("Updated API token: {}", token.getName());

        return Optional.of(toResponseDto(token));
    }

    /**
     * Revoke (deactivate) token
     */
    @Transactional
    public boolean revokeToken(Long tokenId, Long organizationId) {
        Optional<ApiToken> tokenOpt = tokenRepository.findByIdAndOrganizationId(tokenId, organizationId);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        ApiToken token = tokenOpt.get();
        token.setActive(false);
        tokenRepository.save(token);
        log.info("Revoked API token: {}", token.getName());

        return true;
    }

    /**
     * Delete token permanently
     */
    @Transactional
    public boolean deleteToken(Long tokenId, Long organizationId) {
        Optional<ApiToken> tokenOpt = tokenRepository.findByIdAndOrganizationId(tokenId, organizationId);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        tokenRepository.delete(tokenOpt.get());
        log.info("Deleted API token: {}", tokenOpt.get().getName());

        return true;
    }

    /**
     * Regenerate token (new secret, same settings)
     */
    @Transactional
    public Optional<ApiTokenCreatedDto> regenerateToken(Long tokenId, Long organizationId) {
        Optional<ApiToken> tokenOpt = tokenRepository.findByIdAndOrganizationId(tokenId, organizationId);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        ApiToken token = tokenOpt.get();

        // Generate new token
        String rawToken = generateSecureToken();
        String tokenHash = passwordEncoder.encode(rawToken);
        String tokenKey = rawToken.substring(0, 16);

        token.setTokenKey(tokenKey);
        token.setTokenHash(tokenHash);
        token = tokenRepository.save(token);

        log.info("Regenerated API token: {}", token.getName());

        return Optional.of(ApiTokenCreatedDto.builder()
                .id(token.getId())
                .token(rawToken)
                .name(token.getName())
                .type(token.getType())
                .scopes(token.getScopes())
                .expiresAt(token.getExpiresAt())
                .message("New token generated. Store it securely.")
                .build());
    }

    /**
     * Get token usage statistics
     */
    public Optional<ApiTokenUsageDto> getTokenUsage(Long tokenId, Long organizationId) {
        Optional<ApiToken> tokenOpt = tokenRepository.findByIdAndOrganizationId(tokenId, organizationId);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        ApiToken token = tokenOpt.get();

        // In production, this would aggregate from a separate usage tracking table
        return Optional.of(ApiTokenUsageDto.builder()
                .tokenId(token.getId())
                .totalRequests(token.getRequestCount())
                .successfulRequests(token.getRequestCount()) // Simplified
                .failedRequests(0L)
                .lastUsedAt(token.getLastUsedAt())
                .requestsByEndpoint(new HashMap<>())
                .requestsByDay(new HashMap<>())
                .build());
    }

    /**
     * Get available API scopes
     */
    public List<ApiScopeDto> getAvailableScopes() {
        return Arrays.asList(
                scopeDto(ApiToken.Scopes.USERS_READ, "Read Users", "Access to read user data", "Users"),
                scopeDto(ApiToken.Scopes.USERS_WRITE, "Write Users", "Create and update users", "Users"),
                scopeDto(ApiToken.Scopes.COURSES_READ, "Read Courses", "Access to read course data", "Courses"),
                scopeDto(ApiToken.Scopes.COURSES_WRITE, "Write Courses", "Create and update courses", "Courses"),
                scopeDto(ApiToken.Scopes.ENROLLMENTS_READ, "Read Enrollments", "Access to enrollment data", "Enrollments"),
                scopeDto(ApiToken.Scopes.ENROLLMENTS_WRITE, "Write Enrollments", "Create and manage enrollments", "Enrollments"),
                scopeDto(ApiToken.Scopes.PROGRESS_READ, "Read Progress", "Access to learning progress", "Progress"),
                scopeDto(ApiToken.Scopes.PROGRESS_WRITE, "Write Progress", "Update learning progress", "Progress"),
                scopeDto(ApiToken.Scopes.REPORTS_READ, "Read Reports", "Access to reports and analytics", "Reports"),
                scopeDto(ApiToken.Scopes.WEBHOOKS_MANAGE, "Manage Webhooks", "Create and manage webhooks", "Webhooks"),
                scopeDto(ApiToken.Scopes.ORGANIZATION_READ, "Read Organization", "Access organization data", "Organization"),
                scopeDto(ApiToken.Scopes.ORGANIZATION_WRITE, "Write Organization", "Update organization settings", "Organization")
        );
    }

    /**
     * Generate secure random token
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        String random = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return TOKEN_PREFIX + random;
    }

    private ApiTokenResponseDto toResponseDto(ApiToken token) {
        return ApiTokenResponseDto.builder()
                .id(token.getId())
                .tokenKeyPrefix(token.getTokenKey().substring(0, 8) + "...")
                .name(token.getName())
                .description(token.getDescription())
                .type(token.getType())
                .scopes(token.getScopes())
                .active(token.isActive())
                .expiresAt(token.getExpiresAt())
                .lastUsedAt(token.getLastUsedAt())
                .lastUsedIp(token.getLastUsedIp())
                .requestCount(token.getRequestCount())
                .rateLimit(token.getRateLimit())
                .allowedIps(token.getAllowedIps())
                .createdAt(token.getCreatedAt())
                .build();
    }

    private ApiScopeDto scopeDto(String scope, String name, String description, String category) {
        return ApiScopeDto.builder()
                .scope(scope)
                .name(name)
                .description(description)
                .category(category)
                .build();
    }
}
