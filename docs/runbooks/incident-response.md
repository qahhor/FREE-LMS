# Runbook: Incident Response

## Overview
Structured approach to handling production incidents for FREE LMS.

---

## Severity Levels

| Level | Description | Response Time | Examples |
|-------|-------------|---------------|----------|
| **P1** | Critical - Platform down | 15 min | Complete outage, data breach |
| **P2** | High - Major feature broken | 1 hour | Auth down, payments failing |
| **P3** | Medium - Degraded service | 4 hours | Slow performance, minor feature broken |
| **P4** | Low - Minor issue | Next business day | UI bug, documentation error |

---

## Incident Response Process

### Phase 1: Detection & Alert (0-5 min)

1. **Acknowledge alert** in PagerDuty
2. **Join incident channel** - #incident-<id>
3. **Initial assessment:**
   ```bash
   # Quick health check
   curl https://api.freelms.com/actuator/health

   # Check all services
   kubectl get pods -n freelms

   # Check recent errors
   kubectl logs -l app=api-gateway -n freelms --since=5m | grep ERROR
   ```

### Phase 2: Triage (5-15 min)

1. **Identify scope:**
   - Which services affected?
   - How many users impacted?
   - Is data at risk?

2. **Check dashboards:**
   - Grafana: https://grafana.freelms.com/d/freelms-overview
   - Jaeger: https://jaeger.freelms.com

3. **Assign roles:**
   - **Incident Commander (IC):** Coordinates response
   - **Technical Lead:** Drives investigation
   - **Communications:** Updates stakeholders

### Phase 3: Mitigation (15-60 min)

**Option A: Rollback**
```bash
# Rollback to previous version
kubectl rollout undo deployment/<service> -n freelms

# Verify
kubectl rollout status deployment/<service> -n freelms
```

**Option B: Scale Up**
```bash
# Increase replicas
kubectl scale deployment/<service> --replicas=5 -n freelms
```

**Option C: Feature Toggle**
```bash
# Disable problematic feature
curl -X POST https://api.freelms.com/admin/feature-flags \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"flag": "new-feature", "enabled": false}'
```

**Option D: Database Fix**
```bash
# Connect to database (read-only first)
kubectl exec -it postgres-0 -n freelms -- psql -U freelms

# Run corrective query
# ALWAYS test in staging first!
```

### Phase 4: Resolution

1. **Verify fix:**
   ```bash
   # Health check
   curl https://api.freelms.com/actuator/health

   # Test critical flows
   ./integration-tests/run-smoke-tests.sh
   ```

2. **Monitor for 30 minutes**

3. **Update status page**

4. **Close incident in PagerDuty**

### Phase 5: Post-Incident

Within 48 hours:
- [ ] Write incident report
- [ ] Schedule blameless postmortem
- [ ] Create follow-up tickets
- [ ] Update runbooks if needed

---

## Communication Templates

### Initial Response (Internal)
```
🚨 INCIDENT DECLARED - P<severity>
Issue: <brief description>
Impact: <user impact>
IC: <name>
Channel: #incident-<id>
Status: Investigating
```

### Customer Communication
```
We are currently experiencing issues with <service>.
Our team is actively investigating.
We will provide updates every 30 minutes.
Status page: https://status.freelms.com
```

### Resolution
```
✅ INCIDENT RESOLVED
Duration: <time>
Root cause: <brief>
Impact: <metrics>
Follow-up: <actions>
```

---

## Quick Reference Commands

```bash
# Get all unhealthy pods
kubectl get pods -n freelms | grep -v Running

# Recent errors across all services
kubectl logs -l app.kubernetes.io/part-of=freelms -n freelms --since=10m | grep -i error

# Database connections
kubectl exec -it postgres-0 -n freelms -- psql -U freelms -c "SELECT count(*) FROM pg_stat_activity;"

# Redis status
kubectl exec -it redis-0 -n freelms -- redis-cli INFO

# Kafka consumer lag
kubectl exec -it kafka-0 -n freelms -- kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --all-groups
```

---

## Escalation Path

1. **On-Call Engineer** - First responder
2. **Team Lead** - If not resolved in 30 min
3. **Engineering Manager** - P1 incidents
4. **CTO** - Major outages, security incidents
