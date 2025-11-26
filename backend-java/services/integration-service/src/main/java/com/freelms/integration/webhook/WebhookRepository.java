package com.freelms.integration.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Smartup LMS - Webhook Repository
 */
@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    /**
     * Find webhook by ID and organization
     */
    Optional<Webhook> findByWebhookIdAndOrganizationId(String webhookId, Long organizationId);

    /**
     * Find all webhooks for organization
     */
    List<Webhook> findByOrganizationId(Long organizationId);

    /**
     * Find all active webhooks for organization
     */
    List<Webhook> findByOrganizationIdAndActiveTrue(Long organizationId);

    /**
     * Find webhook by URL
     */
    Optional<Webhook> findByOrganizationIdAndUrl(Long organizationId, String url);

    /**
     * Count webhooks for organization
     */
    long countByOrganizationId(Long organizationId);

    /**
     * Check if webhook exists
     */
    boolean existsByWebhookId(String webhookId);
}
