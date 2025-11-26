# FREE LMS - Disaster Recovery Plan

## Overview

This document outlines the disaster recovery procedures for FREE LMS platform designed for **200 clients** and **100,000 users**.

### Recovery Objectives

| Metric | Target | Description |
|--------|--------|-------------|
| **RTO** (Recovery Time Objective) | 4 hours | Maximum acceptable downtime |
| **RPO** (Recovery Point Objective) | 1 hour | Maximum acceptable data loss |
| **MTTR** (Mean Time To Recovery) | 2 hours | Average recovery time |

---

## 1. Backup Strategy

### 1.1 Backup Schedule

| Component | Frequency | Retention | Storage |
|-----------|-----------|-----------|---------|
| PostgreSQL (Full) | Daily at 02:00 UTC | 30 days | S3/GCS + Local |
| PostgreSQL (Weekly) | Sunday 02:00 UTC | 12 weeks | S3/GCS |
| PostgreSQL (Monthly) | 1st of month | 12 months | S3/GCS + Glacier |
| Redis | Every 6 hours | 7 days | S3/GCS |
| Application configs | On change | 90 days | Git + S3 |
| Uploaded files | Real-time sync | Indefinite | S3 Multi-region |

### 1.2 Backup Locations

```
Primary:   s3://freelms-backups-primary/
Secondary: gs://freelms-backups-secondary/
Archive:   s3://freelms-backups-archive/ (Glacier)
```

### 1.3 Automated Backup Verification

```bash
# Cron schedule for backup verification
0 6 * * * /opt/freelms/backup/scripts/verify-backups.sh
```

---

## 2. Disaster Scenarios

### 2.1 Scenario Matrix

| Scenario | Severity | RTO | RPO | Procedure |
|----------|----------|-----|-----|-----------|
| Single service failure | Low | 5 min | 0 | Auto-recovery (K8s) |
| Database corruption | Medium | 1 hour | 1 hour | Point-in-time recovery |
| Complete datacenter failure | High | 4 hours | 1 hour | Failover to secondary |
| Ransomware/Security breach | Critical | 8 hours | 24 hours | Clean restore |
| Data deletion (accidental) | Medium | 30 min | 1 hour | Selective restore |

---

## 3. Recovery Procedures

### 3.1 Single Service Failure

**Symptoms:**
- Service health check failing
- Kubernetes pod in CrashLoopBackOff
- Service unavailable in Eureka

**Automatic Recovery:**
Kubernetes automatically restarts failed pods. Monitor via:

```bash
kubectl get pods -n freelms -w
kubectl describe pod <pod-name> -n freelms
```

**Manual Recovery (if auto-recovery fails):**

```bash
# 1. Check pod status and logs
kubectl logs <pod-name> -n freelms --tail=100

# 2. Force restart
kubectl delete pod <pod-name> -n freelms

# 3. If persistent, rollback deployment
kubectl rollout undo deployment/<service-name> -n freelms

# 4. Check service registry
curl http://service-registry:8761/eureka/apps
```

---

### 3.2 Database Corruption Recovery

**Symptoms:**
- PostgreSQL errors in logs
- Data integrity check failures
- Application errors accessing data

**Step 1: Assess Damage**

```bash
# Connect to database
psql -h $DB_HOST -U freelms -d freelms

# Check for corruption
SELECT datname, checksum_failures FROM pg_stat_database;

# Check table integrity
SELECT schemaname, tablename
FROM pg_tables
WHERE schemaname = 'public';
```

**Step 2: Stop Write Operations**

```bash
# Scale down services that write to database
kubectl scale deployment auth-service --replicas=0 -n freelms
kubectl scale deployment course-service --replicas=0 -n freelms
kubectl scale deployment enrollment-service --replicas=0 -n freelms
# ... scale down other services
```

**Step 3: Point-in-Time Recovery**

```bash
# List available backups
ls -la /var/backups/freelms/postgres/daily/

# Or from cloud
aws s3 ls s3://freelms-backups-primary/postgres/daily/

# Restore to point in time
./postgres-restore.sh \
  --target-db freelms_recovery \
  /var/backups/freelms/postgres/daily/freelms_daily_YYYYMMDD_HHMMSS.sql.gz

# Verify restored data
psql -h $DB_HOST -U freelms -d freelms_recovery \
  -c "SELECT COUNT(*) FROM users;"
```

**Step 4: Swap Databases**

```bash
# Rename databases (during maintenance window)
psql -h $DB_HOST -U postgres << EOF
ALTER DATABASE freelms RENAME TO freelms_corrupted;
ALTER DATABASE freelms_recovery RENAME TO freelms;
EOF

# Restart services
kubectl scale deployment --all --replicas=2 -n freelms
```

---

### 3.3 Complete Datacenter Failure

**Prerequisites:**
- Secondary region infrastructure provisioned
- DNS failover configured
- Database replication active

**Step 1: Declare Disaster**

```bash
# Notify team via PagerDuty/Slack
./notify-disaster.sh "Primary datacenter failure - initiating DR"
```

**Step 2: Verify Secondary Region**

```bash
# Check secondary infrastructure
kubectl --context=secondary-cluster get nodes
kubectl --context=secondary-cluster get pods -n freelms

# Verify database replica status
psql -h $SECONDARY_DB_HOST -U freelms -d freelms \
  -c "SELECT pg_is_in_recovery();"
```

**Step 3: Promote Secondary Database**

```bash
# Promote replica to primary
psql -h $SECONDARY_DB_HOST -U postgres \
  -c "SELECT pg_promote();"

# Verify promotion
psql -h $SECONDARY_DB_HOST -U freelms -d freelms \
  -c "SELECT pg_is_in_recovery();"  # Should return 'f'
```

**Step 4: Update DNS**

```bash
# Route53 failover (if not automatic)
aws route53 change-resource-record-sets \
  --hosted-zone-id $HOSTED_ZONE_ID \
  --change-batch file://dns-failover.json

# Or Cloudflare
curl -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records/$RECORD_ID" \
  -H "Authorization: Bearer $CF_TOKEN" \
  -H "Content-Type: application/json" \
  --data '{"content":"<secondary-ip>"}'
```

**Step 5: Verify Services**

```bash
# Health check all services
for service in gateway auth course enrollment; do
  curl -s https://api.freelms.com/$service/actuator/health | jq .status
done

# Monitor traffic shift
kubectl --context=secondary-cluster logs -f deployment/api-gateway -n freelms
```

---

### 3.4 Ransomware/Security Breach Recovery

**CRITICAL: Do not pay ransom. Follow this procedure.**

**Step 1: Isolate Systems**

```bash
# Immediately isolate affected systems
kubectl cordon --all nodes
kubectl scale deployment --all --replicas=0 -n freelms

# Block all external access
kubectl apply -f emergency-network-policy.yaml
```

**Step 2: Preserve Evidence**

```bash
# Snapshot current state for forensics
./snapshot-forensics.sh

# Export logs
kubectl logs --all-containers --since=24h -n freelms > incident-logs.txt
```

**Step 3: Clean Restore from Known Good Backup**

```bash
# Identify last known good backup (before breach)
# Review backup integrity and scan for malware

# Create fresh infrastructure
terraform apply -var="environment=recovery"

# Restore from verified clean backup
./postgres-restore.sh --drop-existing \
  s3://freelms-backups-archive/postgres/monthly/freelms_monthly_YYYYMMDD.sql.gz
```

**Step 4: Security Hardening Before Bringing Online**

```bash
# Rotate all credentials
./rotate-all-secrets.sh

# Update all service images to latest patched versions
kubectl set image deployment/auth-service \
  auth-service=freelms/auth-service:latest-patched -n freelms

# Enable enhanced logging
kubectl apply -f enhanced-audit-logging.yaml
```

---

### 3.5 Accidental Data Deletion Recovery

**For specific table/data recovery:**

```bash
# 1. Stop writes to affected table
# 2. Create recovery database
createdb -h $DB_HOST -U freelms freelms_recovery

# 3. Restore full backup to recovery database
./postgres-restore.sh --target-db freelms_recovery backup.sql.gz

# 4. Extract specific data
psql -h $DB_HOST -U freelms << EOF
-- Copy deleted data from recovery
INSERT INTO freelms.users
SELECT * FROM freelms_recovery.users
WHERE id NOT IN (SELECT id FROM freelms.users);
EOF

# 5. Cleanup
dropdb -h $DB_HOST -U freelms freelms_recovery
```

---

## 4. Communication Plan

### 4.1 Escalation Matrix

| Severity | Response Time | Notify |
|----------|---------------|--------|
| P1 (Critical) | 15 minutes | CTO, VP Eng, On-call |
| P2 (High) | 1 hour | Engineering Lead, On-call |
| P3 (Medium) | 4 hours | On-call team |
| P4 (Low) | Next business day | Engineering team |

### 4.2 Status Page Updates

```bash
# Update status page
curl -X POST https://api.statuspage.io/v1/pages/$PAGE_ID/incidents \
  -H "Authorization: OAuth $STATUSPAGE_TOKEN" \
  -d '{
    "incident": {
      "name": "Database Recovery in Progress",
      "status": "investigating",
      "impact_override": "major",
      "body": "We are currently recovering from a database issue. ETA: 2 hours."
    }
  }'
```

---

## 5. Testing Schedule

### 5.1 DR Drill Schedule

| Test Type | Frequency | Duration | Participants |
|-----------|-----------|----------|--------------|
| Backup restore test | Monthly | 2 hours | DevOps |
| Service failover test | Quarterly | 4 hours | DevOps + Dev |
| Full DR drill | Annually | 8 hours | All engineering |
| Tabletop exercise | Bi-annually | 2 hours | Leadership + Eng |

### 5.2 DR Drill Checklist

```markdown
- [ ] Notify stakeholders of planned drill
- [ ] Verify backup availability
- [ ] Document start time
- [ ] Execute recovery procedures
- [ ] Verify data integrity
- [ ] Test application functionality
- [ ] Document end time and issues
- [ ] Conduct post-drill review
- [ ] Update procedures based on findings
```

---

## 6. Contact Information

### Emergency Contacts

| Role | Name | Phone | Email |
|------|------|-------|-------|
| Primary On-call | Rotating | PagerDuty | oncall@freelms.com |
| Infrastructure Lead | TBD | TBD | infra@freelms.com |
| Database Admin | TBD | TBD | dba@freelms.com |
| Security Lead | TBD | TBD | security@freelms.com |

### Vendor Support

| Vendor | Support Level | Contact |
|--------|---------------|---------|
| AWS | Enterprise | aws.amazon.com/support |
| PostgreSQL | Community | postgresql.org/support |
| Redis | Enterprise | redis.com/support |

---

## 7. Appendix

### A. Quick Reference Commands

```bash
# Check all service health
kubectl get pods -n freelms -o wide

# View recent logs
kubectl logs -f deployment/api-gateway -n freelms --tail=100

# Database connection
psql -h $DB_HOST -U freelms -d freelms

# Redis connection
redis-cli -h $REDIS_HOST -a $REDIS_PASSWORD

# List backups
aws s3 ls s3://freelms-backups-primary/postgres/ --recursive

# Restore database
./postgres-restore.sh backup.sql.gz
```

### B. Runbook Links

- [Service Restart Runbook](./runbooks/service-restart.md)
- [Database Failover Runbook](./runbooks/database-failover.md)
- [Security Incident Runbook](./runbooks/security-incident.md)

---

**Document Version:** 1.0
**Last Updated:** 2024-01-01
**Next Review:** 2024-04-01
**Owner:** DevOps Team
