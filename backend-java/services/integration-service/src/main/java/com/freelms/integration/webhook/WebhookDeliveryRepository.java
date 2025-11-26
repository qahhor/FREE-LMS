package com.freelms.integration.webhook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Smartup LMS - Webhook Delivery Repository
 */
@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    /**
     * Find delivery by ID
     */
    Optional<WebhookDelivery> findByDeliveryId(String deliveryId);

    /**
     * Find deliveries for webhook ordered by creation date
     */
    List<WebhookDelivery> findByWebhookOrderByCreatedAtDesc(Webhook webhook);

    /**
     * Find deliveries by event ID
     */
    List<WebhookDelivery> findByEventId(String eventId);

    /**
     * Find failed deliveries for retry
     */
    @Query("SELECT d FROM WebhookDelivery d WHERE d.status = 'FAILED' AND d.attemptNumber < :maxAttempts")
    List<WebhookDelivery> findFailedDeliveriesForRetry(int maxAttempts);

    /**
     * Find pending deliveries
     */
    List<WebhookDelivery> findByStatus(WebhookDelivery.DeliveryStatus status);

    /**
     * Count deliveries by status for webhook
     */
    long countByWebhookAndStatus(Webhook webhook, WebhookDelivery.DeliveryStatus status);

    /**
     * Delete old deliveries
     */
    void deleteByCreatedAtBefore(Instant before);

    /**
     * Count deliveries in time range
     */
    @Query("SELECT COUNT(d) FROM WebhookDelivery d WHERE d.webhook = :webhook AND d.createdAt BETWEEN :start AND :end")
    long countDeliveriesInTimeRange(Webhook webhook, Instant start, Instant end);
}
