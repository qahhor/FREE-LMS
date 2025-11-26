# FREE LMS - Feature Flags

Feature flag system for controlled feature rollouts and A/B testing.

## Features

- Boolean, string, number, and JSON flag types
- User and organization targeting
- Percentage-based rollouts
- Gradual rollouts with date ranges
- Real-time updates via Redis
- REST API for evaluation and management

## Quick Start

### Evaluate a Flag

```java
@Autowired
private FeatureFlagService featureFlagService;

public void example() {
    EvaluationContext context = EvaluationContext.builder()
        .userId("user-123")
        .organizationId("org-456")
        .role("LEARNER")
        .build();

    // Boolean check
    if (featureFlagService.isEnabled("new-dashboard", context)) {
        // Show new dashboard
    }

    // Get string value
    String theme = featureFlagService.getString("ui-theme", context, "light");
}
```

### REST API

```bash
# Evaluate single flag
GET /api/feature-flags/new-dashboard

# Evaluate multiple flags
POST /api/feature-flags/evaluate
{
  "flags": ["new-dashboard", "dark-mode", "ai-features"],
  "context": {
    "country": "US",
    "platform": "web"
  }
}

# Create flag (admin)
POST /api/feature-flags
{
  "key": "new-feature",
  "name": "New Feature",
  "type": "BOOLEAN",
  "enabled": true,
  "defaultValue": false,
  "rollout": {
    "type": "PERCENTAGE",
    "percentage": 10
  }
}
```

## Flag Configuration

### Targeting Rules

```json
{
  "key": "premium-features",
  "targetingRules": [
    {
      "id": "enterprise-users",
      "conditions": [
        {"attribute": "role", "operator": "EQUALS", "value": "ENTERPRISE"}
      ],
      "value": true
    },
    {
      "id": "beta-testers",
      "conditions": [
        {"attribute": "email", "operator": "ENDS_WITH", "value": "@beta.freelms.com"}
      ],
      "value": true
    }
  ],
  "defaultValue": false
}
```

### Gradual Rollout

```json
{
  "key": "new-ui",
  "rollout": {
    "type": "GRADUAL",
    "startDate": "2024-01-01T00:00:00Z",
    "endDate": "2024-01-31T23:59:59Z",
    "percentage": 100
  }
}
```

## Best Practices

1. **Use descriptive flag keys**: `payment-v2`, `ai-recommendations`
2. **Set default values**: Always have a safe default
3. **Clean up old flags**: Remove flags after full rollout
4. **Monitor flag usage**: Track evaluation counts
5. **Test both states**: Ensure code works with flag on/off
