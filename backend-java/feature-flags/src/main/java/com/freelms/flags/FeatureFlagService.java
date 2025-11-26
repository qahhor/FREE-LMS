package com.freelms.flags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Smartup LMS - Feature Flag Service
 *
 * Evaluates feature flags with support for:
 * - Boolean, string, number, and JSON flags
 * - User and organization targeting
 * - Percentage-based rollouts
 * - Gradual rollouts with date ranges
 * - Real-time updates via Redis pub/sub
 */
@Service
public class FeatureFlagService {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagService.class);
    private static final String CACHE_PREFIX = "feature_flags:";

    private final RedisTemplate<String, FeatureFlag> redisTemplate;
    private final Map<String, FeatureFlag> localCache = new ConcurrentHashMap<>();

    public FeatureFlagService(RedisTemplate<String, FeatureFlag> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Evaluate a boolean feature flag
     */
    public boolean isEnabled(String flagKey, EvaluationContext context) {
        Object value = evaluate(flagKey, context);
        return value instanceof Boolean && (Boolean) value;
    }

    /**
     * Evaluate a feature flag and return its value
     */
    public Object evaluate(String flagKey, EvaluationContext context) {
        FeatureFlag flag = getFlag(flagKey);

        if (flag == null) {
            log.debug("Flag not found: {}, returning null", flagKey);
            return null;
        }

        if (!flag.isEnabled()) {
            log.debug("Flag disabled: {}, returning default value", flagKey);
            return flag.getDefaultValue();
        }

        // Check rollout configuration
        if (!isInRollout(flag, context)) {
            log.debug("User not in rollout for flag: {}", flagKey);
            return flag.getDefaultValue();
        }

        // Evaluate targeting rules
        if (flag.getTargetingRules() != null && !flag.getTargetingRules().isEmpty()) {
            for (TargetingRule rule : flag.getTargetingRules()) {
                if (evaluateRule(rule, context)) {
                    log.debug("Flag {} matched rule {}", flagKey, rule.getId());
                    return rule.getValue();
                }
            }
        }

        return flag.getDefaultValue();
    }

    /**
     * Get flag value as string
     */
    public String getString(String flagKey, EvaluationContext context, String defaultValue) {
        Object value = evaluate(flagKey, context);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Get flag value as number
     */
    public Number getNumber(String flagKey, EvaluationContext context, Number defaultValue) {
        Object value = evaluate(flagKey, context);
        if (value instanceof Number) {
            return (Number) value;
        }
        return defaultValue;
    }

    /**
     * Check if user is in the rollout
     */
    private boolean isInRollout(FeatureFlag flag, EvaluationContext context) {
        RolloutConfig rollout = flag.getRollout();
        if (rollout == null) {
            return true;
        }

        // Check date range
        Instant now = Instant.now();
        if (rollout.getStartDate() != null && now.isBefore(rollout.getStartDate())) {
            return false;
        }
        if (rollout.getEndDate() != null && now.isAfter(rollout.getEndDate())) {
            return false;
        }

        // Check excluded users
        if (rollout.getExcludedUsers() != null &&
            rollout.getExcludedUsers().contains(context.getUserId())) {
            return false;
        }

        switch (rollout.getType()) {
            case ALL:
                return true;

            case PERCENTAGE:
                return isInPercentage(context.getUserId(), flag.getKey(), rollout.getPercentage());

            case USER_LIST:
                return rollout.getIncludedUsers() != null &&
                       rollout.getIncludedUsers().contains(context.getUserId());

            case ORGANIZATION_LIST:
                return rollout.getIncludedOrganizations() != null &&
                       rollout.getIncludedOrganizations().contains(context.getOrganizationId());

            case GRADUAL:
                return isInGradualRollout(flag, rollout, context);

            default:
                return true;
        }
    }

    /**
     * Deterministic percentage check using consistent hashing
     */
    private boolean isInPercentage(String userId, String flagKey, int percentage) {
        if (userId == null || percentage <= 0) return false;
        if (percentage >= 100) return true;

        String hashInput = flagKey + ":" + userId;
        int hash = Math.abs(hashString(hashInput));
        int bucket = hash % 100;

        return bucket < percentage;
    }

    /**
     * Gradual rollout based on time
     */
    private boolean isInGradualRollout(FeatureFlag flag, RolloutConfig rollout, EvaluationContext context) {
        if (rollout.getStartDate() == null || rollout.getEndDate() == null) {
            return isInPercentage(context.getUserId(), flag.getKey(), rollout.getPercentage());
        }

        Instant now = Instant.now();
        long totalDuration = rollout.getEndDate().toEpochMilli() - rollout.getStartDate().toEpochMilli();
        long elapsed = now.toEpochMilli() - rollout.getStartDate().toEpochMilli();

        if (elapsed < 0) return false;
        if (elapsed >= totalDuration) return true;

        int currentPercentage = (int) ((elapsed * 100) / totalDuration);
        return isInPercentage(context.getUserId(), flag.getKey(), currentPercentage);
    }

    /**
     * Evaluate a targeting rule
     */
    private boolean evaluateRule(TargetingRule rule, EvaluationContext context) {
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            return true;
        }

        // All conditions must match (AND logic)
        for (Condition condition : rule.getConditions()) {
            if (!evaluateCondition(condition, context)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Evaluate a single condition
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(Condition condition, EvaluationContext context) {
        Object contextValue = context.getAttribute(condition.getAttribute());
        Object conditionValue = condition.getValue();

        if (contextValue == null) {
            return condition.getOperator() == Condition.Operator.NOT_EQUALS ||
                   condition.getOperator() == Condition.Operator.NOT_IN ||
                   condition.getOperator() == Condition.Operator.NOT_CONTAINS;
        }

        String contextStr = contextValue.toString();
        String conditionStr = conditionValue != null ? conditionValue.toString() : "";

        switch (condition.getOperator()) {
            case EQUALS:
                return contextStr.equals(conditionStr);
            case NOT_EQUALS:
                return !contextStr.equals(conditionStr);
            case CONTAINS:
                return contextStr.contains(conditionStr);
            case NOT_CONTAINS:
                return !contextStr.contains(conditionStr);
            case STARTS_WITH:
                return contextStr.startsWith(conditionStr);
            case ENDS_WITH:
                return contextStr.endsWith(conditionStr);
            case GREATER_THAN:
                return compareNumbers(contextValue, conditionValue) > 0;
            case LESS_THAN:
                return compareNumbers(contextValue, conditionValue) < 0;
            case IN:
                if (conditionValue instanceof List) {
                    return ((List<Object>) conditionValue).contains(contextValue);
                }
                return false;
            case NOT_IN:
                if (conditionValue instanceof List) {
                    return !((List<Object>) conditionValue).contains(contextValue);
                }
                return true;
            case MATCHES_REGEX:
                return Pattern.matches(conditionStr, contextStr);
            default:
                return false;
        }
    }

    private int compareNumbers(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        }
        return 0;
    }

    private int hashString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Math.abs(java.nio.ByteBuffer.wrap(digest).getInt());
        } catch (Exception e) {
            return input.hashCode();
        }
    }

    /**
     * Get flag from cache or Redis
     */
    private FeatureFlag getFlag(String flagKey) {
        // Check local cache first
        FeatureFlag cached = localCache.get(flagKey);
        if (cached != null) {
            return cached;
        }

        // Fetch from Redis
        FeatureFlag flag = redisTemplate.opsForValue().get(CACHE_PREFIX + flagKey);
        if (flag != null) {
            localCache.put(flagKey, flag);
        }
        return flag;
    }

    /**
     * Create or update a feature flag
     */
    public FeatureFlag saveFlag(FeatureFlag flag) {
        flag.setUpdatedAt(Instant.now());
        if (flag.getCreatedAt() == null) {
            flag.setCreatedAt(Instant.now());
        }

        redisTemplate.opsForValue().set(CACHE_PREFIX + flag.getKey(), flag);
        localCache.put(flag.getKey(), flag);

        log.info("Saved feature flag: {}", flag.getKey());
        return flag;
    }

    /**
     * Delete a feature flag
     */
    public void deleteFlag(String flagKey) {
        redisTemplate.delete(CACHE_PREFIX + flagKey);
        localCache.remove(flagKey);
        log.info("Deleted feature flag: {}", flagKey);
    }

    /**
     * Refresh local cache periodically
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void refreshCache() {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                String flagKey = key.substring(CACHE_PREFIX.length());
                FeatureFlag flag = redisTemplate.opsForValue().get(key);
                if (flag != null) {
                    localCache.put(flagKey, flag);
                }
            }
        }
        log.debug("Feature flag cache refreshed, {} flags loaded", localCache.size());
    }
}
