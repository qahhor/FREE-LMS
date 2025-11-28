package com.freelms.lti.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * LTI Launch session tracking.
 */
@Entity
@Table(name = "lti_launches", indexes = {
    @Index(name = "idx_lti_launch_nonce", columnList = "nonce"),
    @Index(name = "idx_lti_launch_state", columnList = "state"),
    @Index(name = "idx_lti_launch_user", columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
public class LtiLaunch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Launch identification
    @Column(nullable = false, unique = true)
    private String nonce;

    @Column(nullable = false)
    private String state;

    // Platform/Tool reference
    private UUID platformId;
    private UUID toolId;

    // Message type
    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.RESOURCE_LINK;

    // User info
    @Column(name = "user_id")
    private Long userId;

    private String ltiUserId;
    private String userName;
    private String userEmail;

    // Context info
    private Long courseId;
    private String ltiContextId;
    private String contextTitle;

    // Resource link
    private String resourceLinkId;
    private String resourceLinkTitle;

    // Roles
    @Column(length = 1000)
    private String roles;

    // Deep linking
    private String deepLinkReturnUrl;
    private String deepLinkData;

    // AGS endpoints (if available)
    private String lineItemUrl;
    private String lineItemsUrl;
    private String scoresUrl;

    // NRPS endpoint (if available)
    private String membershipsUrl;

    // Custom parameters
    @Column(length = 2000)
    private String customParams;

    // Launch status
    @Enumerated(EnumType.STRING)
    private LaunchStatus status = LaunchStatus.INITIATED;

    private String errorMessage;

    // Session tracking
    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;

    // IP tracking
    private String ipAddress;
    private String userAgent;

    public enum MessageType {
        RESOURCE_LINK,
        DEEP_LINKING_REQUEST,
        DEEP_LINKING_RESPONSE,
        SUBMISSION_REVIEW
    }

    public enum LaunchStatus {
        INITIATED,
        OIDC_INITIATED,
        TOKEN_VALIDATED,
        COMPLETED,
        FAILED,
        EXPIRED
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public UUID getPlatformId() { return platformId; }
    public void setPlatformId(UUID platformId) { this.platformId = platformId; }

    public UUID getToolId() { return toolId; }
    public void setToolId(UUID toolId) { this.toolId = toolId; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getLtiUserId() { return ltiUserId; }
    public void setLtiUserId(String ltiUserId) { this.ltiUserId = ltiUserId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getLtiContextId() { return ltiContextId; }
    public void setLtiContextId(String ltiContextId) { this.ltiContextId = ltiContextId; }

    public String getContextTitle() { return contextTitle; }
    public void setContextTitle(String contextTitle) { this.contextTitle = contextTitle; }

    public String getResourceLinkId() { return resourceLinkId; }
    public void setResourceLinkId(String resourceLinkId) { this.resourceLinkId = resourceLinkId; }

    public String getResourceLinkTitle() { return resourceLinkTitle; }
    public void setResourceLinkTitle(String resourceLinkTitle) { this.resourceLinkTitle = resourceLinkTitle; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getDeepLinkReturnUrl() { return deepLinkReturnUrl; }
    public void setDeepLinkReturnUrl(String deepLinkReturnUrl) { this.deepLinkReturnUrl = deepLinkReturnUrl; }

    public String getDeepLinkData() { return deepLinkData; }
    public void setDeepLinkData(String deepLinkData) { this.deepLinkData = deepLinkData; }

    public String getLineItemUrl() { return lineItemUrl; }
    public void setLineItemUrl(String lineItemUrl) { this.lineItemUrl = lineItemUrl; }

    public String getLineItemsUrl() { return lineItemsUrl; }
    public void setLineItemsUrl(String lineItemsUrl) { this.lineItemsUrl = lineItemsUrl; }

    public String getScoresUrl() { return scoresUrl; }
    public void setScoresUrl(String scoresUrl) { this.scoresUrl = scoresUrl; }

    public String getMembershipsUrl() { return membershipsUrl; }
    public void setMembershipsUrl(String membershipsUrl) { this.membershipsUrl = membershipsUrl; }

    public String getCustomParams() { return customParams; }
    public void setCustomParams(String customParams) { this.customParams = customParams; }

    public LaunchStatus getStatus() { return status; }
    public void setStatus(LaunchStatus status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
