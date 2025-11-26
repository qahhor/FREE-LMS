# Runbook: Database Operations

## Overview
PostgreSQL database maintenance and troubleshooting procedures.

---

## Daily Health Checks

```bash
# Connect to primary
kubectl exec -it postgres-0 -n freelms -- psql -U freelms

# Check connections
SELECT count(*), state FROM pg_stat_activity GROUP BY state;

# Check database size
SELECT pg_database.datname, pg_size_pretty(pg_database_size(pg_database.datname))
FROM pg_database ORDER BY pg_database_size(pg_database.datname) DESC;

# Check long-running queries (>30 sec)
SELECT pid, now() - pg_stat_activity.query_start AS duration, query
FROM pg_stat_activity
WHERE state = 'active' AND now() - pg_stat_activity.query_start > interval '30 seconds';

# Check replication lag (if replica exists)
SELECT client_addr, state, sent_lsn, write_lsn, flush_lsn, replay_lsn,
       (extract(epoch from now()) - extract(epoch from replay_lag))::int as lag_seconds
FROM pg_stat_replication;
```

---

## Common Operations

### Kill Long-Running Query

```sql
-- Identify the query
SELECT pid, now() - pg_stat_activity.query_start AS duration, query
FROM pg_stat_activity WHERE state = 'active';

-- Cancel query (graceful)
SELECT pg_cancel_backend(<pid>);

-- Terminate connection (forceful)
SELECT pg_terminate_backend(<pid>);
```

### Check Table Bloat

```sql
SELECT schemaname, tablename,
       pg_size_pretty(pg_total_relation_size(schemaname || '.' || tablename)) as total_size,
       pg_size_pretty(pg_table_size(schemaname || '.' || tablename)) as table_size,
       pg_size_pretty(pg_indexes_size(schemaname || '.' || tablename)) as index_size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname || '.' || tablename) DESC
LIMIT 20;
```

### Vacuum Table

```sql
-- Regular vacuum
VACUUM VERBOSE <table_name>;

-- Full vacuum (locks table - use during maintenance window)
VACUUM FULL VERBOSE <table_name>;

-- Analyze to update statistics
ANALYZE <table_name>;
```

### Reindex

```sql
-- Reindex single index
REINDEX INDEX <index_name>;

-- Reindex table (all indexes)
REINDEX TABLE <table_name>;

-- Reindex concurrently (no locks, PostgreSQL 12+)
REINDEX TABLE CONCURRENTLY <table_name>;
```

---

## Backup Operations

### Manual Backup

```bash
# Run backup script
/opt/freelms/backup/scripts/postgres-backup.sh

# Verify backup
ls -la /var/backups/freelms/postgres/daily/

# Check backup integrity
gzip -t /var/backups/freelms/postgres/daily/freelms_daily_*.sql.gz
```

### Restore from Backup

```bash
# List available backups
ls -la /var/backups/freelms/postgres/

# Restore (see backup/scripts/postgres-restore.sh)
./postgres-restore.sh --verify-only <backup-file>
./postgres-restore.sh <backup-file>
```

---

## Emergency Procedures

### Connection Pool Exhausted

```bash
# 1. Check current connections
kubectl exec -it postgres-0 -n freelms -- psql -U freelms -c \
  "SELECT count(*) FROM pg_stat_activity;"

# 2. Identify connection hogs
kubectl exec -it postgres-0 -n freelms -- psql -U freelms -c \
  "SELECT application_name, count(*) FROM pg_stat_activity GROUP BY application_name ORDER BY count(*) DESC;"

# 3. Kill idle connections older than 10 minutes
kubectl exec -it postgres-0 -n freelms -- psql -U freelms -c \
  "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE state = 'idle' AND query_start < now() - interval '10 minutes';"

# 4. Restart problematic service
kubectl rollout restart deployment/<service-name> -n freelms
```

### Deadlock Detected

```sql
-- Check for locks
SELECT blocked_locks.pid AS blocked_pid,
       blocked_activity.usename AS blocked_user,
       blocking_locks.pid AS blocking_pid,
       blocking_activity.usename AS blocking_user,
       blocked_activity.query AS blocked_statement,
       blocking_activity.query AS blocking_statement
FROM pg_catalog.pg_locks blocked_locks
JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
JOIN pg_catalog.pg_locks blocking_locks ON blocking_locks.locktype = blocked_locks.locktype
JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
WHERE NOT blocked_locks.granted;

-- Terminate blocking query
SELECT pg_terminate_backend(<blocking_pid>);
```

### Disk Space Low

```bash
# 1. Check disk usage
kubectl exec -it postgres-0 -n freelms -- df -h

# 2. Check table sizes
kubectl exec -it postgres-0 -n freelms -- psql -U freelms -c \
  "SELECT relname, pg_size_pretty(pg_total_relation_size(relid)) FROM pg_catalog.pg_statio_user_tables ORDER BY pg_total_relation_size(relid) DESC LIMIT 10;"

# 3. Clean up WAL files (if safe)
kubectl exec -it postgres-0 -n freelms -- psql -U freelms -c \
  "SELECT pg_switch_wal();"

# 4. Vacuum to reclaim space
kubectl exec -it postgres-0 -n freelms -- psql -U freelms -c \
  "VACUUM FULL;"
```

---

## Index Management

### Check Missing Indexes

```sql
-- Tables with sequential scans (potential index candidates)
SELECT schemaname, relname, seq_scan, seq_tup_read, idx_scan, idx_tup_fetch
FROM pg_stat_user_tables
WHERE seq_scan > 1000 AND seq_tup_read / seq_scan > 1000
ORDER BY seq_tup_read DESC;

-- Unused indexes (candidates for removal)
SELECT indexrelname, idx_scan, pg_size_pretty(pg_relation_size(indexrelid))
FROM pg_stat_user_indexes
WHERE idx_scan = 0 AND indexrelname NOT LIKE '%pkey%'
ORDER BY pg_relation_size(indexrelid) DESC;
```

### Create Index Concurrently

```sql
-- Create index without locking table
CREATE INDEX CONCURRENTLY idx_users_email ON users(email);

-- Drop unused index
DROP INDEX CONCURRENTLY idx_old_index;
```

---

## Monitoring Queries

```sql
-- Slowest queries
SELECT query, calls, mean_time, total_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;

-- Cache hit ratio (should be > 99%)
SELECT sum(heap_blks_hit) / (sum(heap_blks_hit) + sum(heap_blks_read)) as ratio
FROM pg_statio_user_tables;

-- Index usage ratio
SELECT relname, idx_scan, seq_scan,
       100 * idx_scan / (seq_scan + idx_scan) as idx_usage_percent
FROM pg_stat_user_tables
WHERE seq_scan + idx_scan > 0
ORDER BY idx_usage_percent ASC;
```
