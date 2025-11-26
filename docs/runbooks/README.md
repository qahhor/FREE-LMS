# FREE LMS - Operations Runbooks

This directory contains operational runbooks for managing the FREE LMS platform.

## Quick Links

| Runbook | Description | Severity |
|---------|-------------|----------|
| [Service Restart](service-restart.md) | Restarting individual services | Low |
| [Database Operations](database-operations.md) | DB maintenance and troubleshooting | Medium |
| [Incident Response](incident-response.md) | Handling production incidents | High |
| [Scaling Operations](scaling-operations.md) | Manual and auto-scaling | Medium |
| [Deployment](deployment.md) | Deploying new versions | Medium |

## On-Call Checklist

Before starting on-call shift:

- [ ] Access to Grafana dashboards
- [ ] Access to Kibana/logs
- [ ] Access to Kubernetes cluster
- [ ] Access to PagerDuty
- [ ] Slack channels joined (#ops-alerts, #oncall)
- [ ] VPN connected
- [ ] Runbooks bookmarked

## Contact Information

| Role | Contact |
|------|---------|
| Primary On-Call | PagerDuty rotation |
| Secondary On-Call | PagerDuty rotation |
| Infrastructure Lead | infra@freelms.com |
| Database Admin | dba@freelms.com |
