# Runbook: Service Restart

## Overview
Procedures for restarting FREE LMS microservices safely.

## When to Use
- Service is unresponsive
- Memory leak detected
- After configuration change
- After security patch

---

## Pre-Restart Checklist

- [ ] Check service health in Grafana
- [ ] Verify no active critical operations
- [ ] Notify team in #ops-alerts
- [ ] Check replica count (ensure > 1)

---

## Procedure

### 1. Single Pod Restart (Kubernetes)

```bash
# Get pod name
kubectl get pods -n freelms -l app=<service-name>

# Delete single pod (Kubernetes will recreate)
kubectl delete pod <pod-name> -n freelms

# Watch recreation
kubectl get pods -n freelms -l app=<service-name> -w
```

### 2. Rolling Restart (All Replicas)

```bash
# Trigger rolling restart
kubectl rollout restart deployment/<service-name> -n freelms

# Watch progress
kubectl rollout status deployment/<service-name> -n freelms

# Verify all pods healthy
kubectl get pods -n freelms -l app=<service-name>
```

### 3. Restart Specific Services

#### API Gateway
```bash
kubectl rollout restart deployment/api-gateway -n freelms
# Wait 2 minutes before checking traffic
```

#### Auth Service
```bash
# WARNING: Active sessions may be affected
kubectl rollout restart deployment/auth-service -n freelms
```

#### Course Service
```bash
kubectl rollout restart deployment/course-service -n freelms
```

---

## Verification

After restart, verify:

```bash
# 1. Pod status
kubectl get pods -n freelms -l app=<service-name>
# All pods should be Running, 1/1 Ready

# 2. Health endpoint
curl https://api.freelms.com/<service>/actuator/health
# Should return {"status":"UP"}

# 3. Check logs for errors
kubectl logs deployment/<service-name> -n freelms --tail=50

# 4. Check Grafana dashboard
# - Error rate should be < 1%
# - Response time normal
```

---

## Rollback

If service fails after restart:

```bash
# Check rollout history
kubectl rollout history deployment/<service-name> -n freelms

# Rollback to previous version
kubectl rollout undo deployment/<service-name> -n freelms

# Verify rollback
kubectl rollout status deployment/<service-name> -n freelms
```

---

## Troubleshooting

### Pod stuck in Pending
```bash
kubectl describe pod <pod-name> -n freelms
# Check Events section for scheduling issues
```

### Pod in CrashLoopBackOff
```bash
# Check logs
kubectl logs <pod-name> -n freelms --previous

# Common causes:
# - Database connection failed
# - Config missing
# - Memory limit too low
```

### Service not receiving traffic
```bash
# Check endpoints
kubectl get endpoints <service-name> -n freelms

# Check service
kubectl describe service <service-name> -n freelms
```
