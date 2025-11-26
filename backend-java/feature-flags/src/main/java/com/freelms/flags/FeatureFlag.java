package com.freelms.flags;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Smartup LMS - Feature Flag Entity
 *
 * Represents a feature flag with targeting rules and rollout configuration.
 */
public class FeatureFlag {

    private String key;
    private String name;
    private String description;
    private boolean enabled;
    private FlagType type;
    private Object defaultValue;
    private List<TargetingRule> targetingRules;
    private RolloutConfig rollout;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;

    public enum FlagType {
        BOOLEAN,
        STRING,
        NUMBER,
        JSON
    }

    // Getters and setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public FlagType getType() { return type; }
    public void setType(FlagType type) { this.type = type; }

    public Object getDefaultValue() { return defaultValue; }
    public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }

    public List<TargetingRule> getTargetingRules() { return targetingRules; }
    public void setTargetingRules(List<TargetingRule> targetingRules) { this.targetingRules = targetingRules; }

    public RolloutConfig getRollout() { return rollout; }
    public void setRollout(RolloutConfig rollout) { this.rollout = rollout; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}

/**
 * Targeting rule for conditional flag evaluation
 */
class TargetingRule {
    private String id;
    private List<Condition> conditions;
    private Object value;
    private int priority;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<Condition> getConditions() { return conditions; }
    public void setConditions(List<Condition> conditions) { this.conditions = conditions; }

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}

/**
 * Condition for targeting rules
 */
class Condition {
    private String attribute;
    private Operator operator;
    private Object value;

    public enum Operator {
        EQUALS,
        NOT_EQUALS,
        CONTAINS,
        NOT_CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        GREATER_THAN,
        LESS_THAN,
        IN,
        NOT_IN,
        MATCHES_REGEX
    }

    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }

    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
}

/**
 * Rollout configuration for gradual feature releases
 */
class RolloutConfig {
    private RolloutType type;
    private int percentage;
    private List<String> includedUsers;
    private List<String> excludedUsers;
    private List<String> includedOrganizations;
    private Instant startDate;
    private Instant endDate;

    public enum RolloutType {
        ALL,
        PERCENTAGE,
        USER_LIST,
        ORGANIZATION_LIST,
        GRADUAL
    }

    public RolloutType getType() { return type; }
    public void setType(RolloutType type) { this.type = type; }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }

    public List<String> getIncludedUsers() { return includedUsers; }
    public void setIncludedUsers(List<String> includedUsers) { this.includedUsers = includedUsers; }

    public List<String> getExcludedUsers() { return excludedUsers; }
    public void setExcludedUsers(List<String> excludedUsers) { this.excludedUsers = excludedUsers; }

    public List<String> getIncludedOrganizations() { return includedOrganizations; }
    public void setIncludedOrganizations(List<String> includedOrganizations) { this.includedOrganizations = includedOrganizations; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }
}
