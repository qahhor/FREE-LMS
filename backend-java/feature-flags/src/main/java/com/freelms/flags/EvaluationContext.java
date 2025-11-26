package com.freelms.flags;

import java.util.HashMap;
import java.util.Map;

/**
 * Smartup LMS - Feature Flag Evaluation Context
 *
 * Contains user and environment information for flag evaluation.
 */
public class EvaluationContext {

    private String userId;
    private String organizationId;
    private String role;
    private String email;
    private String country;
    private String language;
    private String platform;  // web, ios, android
    private String appVersion;
    private Map<String, Object> customAttributes;

    private EvaluationContext(Builder builder) {
        this.userId = builder.userId;
        this.organizationId = builder.organizationId;
        this.role = builder.role;
        this.email = builder.email;
        this.country = builder.country;
        this.language = builder.language;
        this.platform = builder.platform;
        this.appVersion = builder.appVersion;
        this.customAttributes = builder.customAttributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get attribute value by name for condition evaluation
     */
    public Object getAttribute(String name) {
        switch (name.toLowerCase()) {
            case "userid":
            case "user_id":
                return userId;
            case "organizationid":
            case "organization_id":
            case "org_id":
                return organizationId;
            case "role":
                return role;
            case "email":
                return email;
            case "country":
                return country;
            case "language":
                return language;
            case "platform":
                return platform;
            case "appversion":
            case "app_version":
                return appVersion;
            default:
                return customAttributes != null ? customAttributes.get(name) : null;
        }
    }

    // Getters
    public String getUserId() { return userId; }
    public String getOrganizationId() { return organizationId; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getCountry() { return country; }
    public String getLanguage() { return language; }
    public String getPlatform() { return platform; }
    public String getAppVersion() { return appVersion; }
    public Map<String, Object> getCustomAttributes() { return customAttributes; }

    /**
     * Builder for EvaluationContext
     */
    public static class Builder {
        private String userId;
        private String organizationId;
        private String role;
        private String email;
        private String country;
        private String language;
        private String platform;
        private String appVersion;
        private Map<String, Object> customAttributes = new HashMap<>();

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder organizationId(String organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder appVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public Builder customAttribute(String key, Object value) {
            this.customAttributes.put(key, value);
            return this;
        }

        public Builder customAttributes(Map<String, Object> attributes) {
            this.customAttributes.putAll(attributes);
            return this;
        }

        public EvaluationContext build() {
            return new EvaluationContext(this);
        }
    }

    /**
     * Create context from current authenticated user
     */
    public static EvaluationContext fromUser(String userId, String organizationId, String role) {
        return builder()
                .userId(userId)
                .organizationId(organizationId)
                .role(role)
                .build();
    }

    /**
     * Anonymous context for unauthenticated users
     */
    public static EvaluationContext anonymous() {
        return builder()
                .userId("anonymous-" + System.currentTimeMillis())
                .role("ANONYMOUS")
                .build();
    }
}
