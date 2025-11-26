# Smartup LMS - Backup & Recovery System

Automated backup and disaster recovery system for Smartup LMS platform.

## Quick Start

### 1. Configure Environment

```bash
# Copy example environment
cp .env.example .env

# Edit with your settings
nano .env
```

### 2. Install Cron Jobs

```bash
# Copy cron configuration
sudo cp crontab.example /etc/cron.d/freelms-backup
sudo chmod 644 /etc/cron.d/freelms-backup
```

### 3. Manual Backup

```bash
# PostgreSQL backup
./scripts/postgres-backup.sh

# Redis backup
./scripts/redis-backup.sh
```

### 4. Restore from Backup

```bash
# List available backups
ls -la /var/backups/freelms/postgres/daily/

# Restore PostgreSQL
./scripts/postgres-restore.sh /var/backups/freelms/postgres/daily/freelms_daily_YYYYMMDD_HHMMSS.sql.gz
```

## Directory Structure

```
backup/
├── scripts/
│   ├── postgres-backup.sh    # PostgreSQL backup script
│   ├── postgres-restore.sh   # PostgreSQL restore script
│   └── redis-backup.sh       # Redis backup script
├── docs/
│   └── DISASTER_RECOVERY.md  # Full DR documentation
├── crontab.example           # Cron schedule template
└── README.md                 # This file
```

## Backup Schedule

| Component | Frequency | Retention |
|-----------|-----------|-----------|
| PostgreSQL Daily | 02:00 UTC | 30 days |
| PostgreSQL Weekly | Sunday | 12 weeks |
| PostgreSQL Monthly | 1st | 12 months |
| Redis | Every 6 hours | 7 days |

## Recovery Objectives

- **RTO** (Recovery Time): 4 hours
- **RPO** (Data Loss): 1 hour

## Documentation

See [DISASTER_RECOVERY.md](docs/DISASTER_RECOVERY.md) for complete procedures.

## Support

- Email: devops@freelms.com
- Slack: #freelms-ops
