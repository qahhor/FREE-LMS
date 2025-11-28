package com.freelms.audit.kafka;

import com.freelms.audit.entity.AuditEvent;
import com.freelms.audit.service.AuditLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer for centralized audit event collection.
 */
@Component
public class AuditEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumer.class);

    private final AuditLoggingService auditService;

    public AuditEventConsumer(AuditLoggingService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(topics = "audit-events", groupId = "audit-logging-service")
    public void handleAuditEvent(Map<String, Object> message) {
        try {
            AuditEvent event = mapToAuditEvent(message);
            auditService.logEvent(event);
        } catch (Exception e) {
            log.error("Failed to process audit event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = {"user-events", "course-events", "enrollment-events",
            "content-events", "assessment-events", "booking-events"},
            groupId = "audit-logging-service")
    public void handleDomainEvent(Map<String, Object> message) {
        try {
            String eventType = (String) message.get("eventType");
            Object data = message.get("data");

            AuditEvent event = new AuditEvent();
            event.setEventType(mapEventType(eventType));
            event.setCategory(mapCategory(eventType));
            event.setAction(eventType);
            event.setDescription("Domain event: " + eventType);

            if (data instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) data;
                if (dataMap.containsKey("userId")) {
                    event.setActorId(((Number) dataMap.get("userId")).longValue());
                }
                if (dataMap.containsKey("organizationId")) {
                    event.setOrganizationId(((Number) dataMap.get("organizationId")).longValue());
                }
                if (dataMap.containsKey("courseId")) {
                    event.setCourseId(((Number) dataMap.get("courseId")).longValue());
                }
            }

            event.setServiceName(extractServiceName(eventType));
            auditService.logEvent(event);

        } catch (Exception e) {
            log.error("Failed to process domain event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "security-events", groupId = "audit-logging-service")
    public void handleSecurityEvent(Map<String, Object> message) {
        try {
            AuditEvent event = new AuditEvent();
            event.setCategory(AuditEvent.EventCategory.SECURITY);
            event.setSeverity(AuditEvent.Severity.WARNING);

            String eventType = (String) message.get("eventType");
            event.setEventType(mapSecurityEventType(eventType));
            event.setAction(eventType);

            if (message.containsKey("userId")) {
                event.setActorId(((Number) message.get("userId")).longValue());
            }
            if (message.containsKey("ipAddress")) {
                event.setIpAddress((String) message.get("ipAddress"));
            }
            if (message.containsKey("description")) {
                event.setDescription((String) message.get("description"));
            }

            auditService.logEvent(event);

        } catch (Exception e) {
            log.error("Failed to process security event: {}", e.getMessage());
        }
    }

    private AuditEvent mapToAuditEvent(Map<String, Object> message) {
        AuditEvent event = new AuditEvent();

        if (message.containsKey("eventType")) {
            event.setEventType(AuditEvent.EventType.valueOf((String) message.get("eventType")));
        }
        if (message.containsKey("category")) {
            event.setCategory(AuditEvent.EventCategory.valueOf((String) message.get("category")));
        }
        if (message.containsKey("action")) {
            event.setAction((String) message.get("action"));
        }
        if (message.containsKey("actorId")) {
            event.setActorId(((Number) message.get("actorId")).longValue());
        }
        if (message.containsKey("organizationId")) {
            event.setOrganizationId(((Number) message.get("organizationId")).longValue());
        }
        if (message.containsKey("resourceType")) {
            event.setResourceType((String) message.get("resourceType"));
        }
        if (message.containsKey("resourceId")) {
            event.setResourceId((String) message.get("resourceId"));
        }
        if (message.containsKey("description")) {
            event.setDescription((String) message.get("description"));
        }
        if (message.containsKey("ipAddress")) {
            event.setIpAddress((String) message.get("ipAddress"));
        }
        if (message.containsKey("containsPii")) {
            event.setContainsPii((Boolean) message.get("containsPii"));
        }

        return event;
    }

    private AuditEvent.EventType mapEventType(String eventType) {
        try {
            return AuditEvent.EventType.valueOf(eventType);
        } catch (IllegalArgumentException e) {
            if (eventType.contains("CREATED")) return AuditEvent.EventType.DATA_CREATED;
            if (eventType.contains("UPDATED")) return AuditEvent.EventType.DATA_UPDATED;
            if (eventType.contains("DELETED")) return AuditEvent.EventType.DATA_DELETED;
            return AuditEvent.EventType.DATA_READ;
        }
    }

    private AuditEvent.EventCategory mapCategory(String eventType) {
        if (eventType.contains("USER")) return AuditEvent.EventCategory.USER_MANAGEMENT;
        if (eventType.contains("COURSE")) return AuditEvent.EventCategory.COURSE_MANAGEMENT;
        if (eventType.contains("ENROLLMENT")) return AuditEvent.EventCategory.COURSE_MANAGEMENT;
        if (eventType.contains("CONTENT")) return AuditEvent.EventCategory.CONTENT_MANAGEMENT;
        if (eventType.contains("QUIZ") || eventType.contains("ASSIGNMENT") || eventType.contains("GRADE"))
            return AuditEvent.EventCategory.ASSESSMENT;
        if (eventType.contains("BOOKING")) return AuditEvent.EventCategory.DATA_MODIFICATION;
        return AuditEvent.EventCategory.DATA_MODIFICATION;
    }

    private AuditEvent.EventType mapSecurityEventType(String eventType) {
        try {
            return AuditEvent.EventType.valueOf(eventType);
        } catch (IllegalArgumentException e) {
            return AuditEvent.EventType.SECURITY_ALERT;
        }
    }

    private String extractServiceName(String eventType) {
        if (eventType.contains("USER")) return "user-service";
        if (eventType.contains("COURSE")) return "course-service";
        if (eventType.contains("ENROLLMENT")) return "enrollment-service";
        if (eventType.contains("CONTENT")) return "content-service";
        if (eventType.contains("QUIZ") || eventType.contains("ASSESSMENT")) return "assessment-service";
        if (eventType.contains("BOOKING")) return "resource-booking-service";
        return "unknown";
    }
}
