package com.freelms.integration.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Smartup LMS - API Token Repository
 */
@Repository
public interface ApiTokenRepository extends JpaRepository<ApiToken, Long> {

    /**
     * Find token by its key prefix (first 16 characters)
     */
    Optional<ApiToken> findByTokenKey(String tokenKey);

    /**
     * Find all tokens for an organization
     */
    List<ApiToken> findByOrganizationId(Long organizationId);

    /**
     * Find token by ID and organization
     */
    Optional<ApiToken> findByIdAndOrganizationId(Long id, Long organizationId);

    /**
     * Find all active tokens for organization
     */
    List<ApiToken> findByOrganizationIdAndActiveTrue(Long organizationId);

    /**
     * Find expired tokens
     */
    @Query("SELECT t FROM ApiToken t WHERE t.expiresAt IS NOT NULL AND t.expiresAt < :now AND t.active = true")
    List<ApiToken> findExpiredTokens(Instant now);

    /**
     * Find tokens by type
     */
    List<ApiToken> findByOrganizationIdAndType(Long organizationId, ApiToken.TokenType type);

    /**
     * Count active tokens for organization
     */
    long countByOrganizationIdAndActiveTrue(Long organizationId);

    /**
     * Find tokens not used since a certain date
     */
    @Query("SELECT t FROM ApiToken t WHERE t.lastUsedAt IS NULL OR t.lastUsedAt < :since")
    List<ApiToken> findUnusedTokensSince(Instant since);
}
