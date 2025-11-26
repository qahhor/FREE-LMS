#!/bin/bash
# ============================================================================
# Smartup LMS - Redis Backup Script
# Automated backup for Redis cache and sessions
# ============================================================================

set -euo pipefail

# =============================================================================
# Configuration
# =============================================================================

REDIS_HOST="${REDIS_HOST:-localhost}"
REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_PASSWORD="${REDIS_PASSWORD:-}"

BACKUP_DIR="${BACKUP_DIR:-/var/backups/freelms/redis}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"

S3_BUCKET="${S3_BUCKET:-}"
S3_REGION="${S3_REGION:-us-east-1}"
GCS_BUCKET="${GCS_BUCKET:-}"

SLACK_WEBHOOK="${SLACK_WEBHOOK:-}"
LOG_FILE="/var/log/freelms/backup-redis.log"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# =============================================================================
# Functions
# =============================================================================

log() {
    local level="$1"
    shift
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] [$level] $*" | tee -a "$LOG_FILE"
}

log_info() { log "INFO" "$@"; }
log_error() { log "ERROR" "$@"; }

redis_cli() {
    local auth_opt=""
    [[ -n "$REDIS_PASSWORD" ]] && auth_opt="-a $REDIS_PASSWORD"
    redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" $auth_opt --no-auth-warning "$@"
}

send_notification() {
    local status="$1"
    local message="$2"

    if [[ -n "$SLACK_WEBHOOK" ]]; then
        local color="good"
        [[ "$status" == "error" ]] && color="danger"

        curl -s -X POST "$SLACK_WEBHOOK" \
            -H 'Content-Type: application/json' \
            -d "{
                \"attachments\": [{
                    \"color\": \"$color\",
                    \"title\": \"Smartup LMS Redis Backup - ${status^^}\",
                    \"text\": \"$message\",
                    \"footer\": \"Smartup LMS Backup System\"
                }]
            }" || true
    fi
}

cleanup_old_backups() {
    log_info "Cleaning up backups older than $RETENTION_DAYS days..."
    find "$BACKUP_DIR" -type f -name "*.rdb*" -mtime +$RETENTION_DAYS -delete 2>/dev/null || true
}

upload_to_cloud() {
    local file="$1"
    local remote_path="$2"

    if [[ -n "$S3_BUCKET" ]]; then
        log_info "Uploading to S3..."
        aws s3 cp "$file" "s3://$S3_BUCKET/$remote_path" --region "$S3_REGION" || true
    fi

    if [[ -n "$GCS_BUCKET" ]]; then
        log_info "Uploading to GCS..."
        gsutil cp "$file" "gs://$GCS_BUCKET/$remote_path" || true
    fi
}

# =============================================================================
# Main
# =============================================================================

main() {
    log_info "=========================================="
    log_info "Smartup LMS Redis Backup Starting"
    log_info "=========================================="

    mkdir -p "$BACKUP_DIR"
    mkdir -p "$(dirname "$LOG_FILE")"

    local backup_file="$BACKUP_DIR/redis_${TIMESTAMP}.rdb"

    # Check Redis connection
    if ! redis_cli PING > /dev/null 2>&1; then
        log_error "Cannot connect to Redis at $REDIS_HOST:$REDIS_PORT"
        send_notification "error" "Redis backup failed - cannot connect"
        exit 1
    fi

    # Get Redis info
    local db_size=$(redis_cli DBSIZE | awk '{print $2}')
    local memory_used=$(redis_cli INFO memory | grep used_memory_human | cut -d: -f2 | tr -d '\r')
    log_info "Redis keys: $db_size, Memory: $memory_used"

    # Trigger BGSAVE
    log_info "Triggering BGSAVE..."
    redis_cli BGSAVE > /dev/null

    # Wait for BGSAVE to complete
    log_info "Waiting for background save to complete..."
    local max_wait=300
    local waited=0
    while [[ $(redis_cli LASTSAVE) == $(redis_cli LASTSAVE) ]] && [[ $waited -lt $max_wait ]]; do
        sleep 1
        ((waited++))

        local status=$(redis_cli INFO persistence | grep rdb_bgsave_in_progress | cut -d: -f2 | tr -d '\r')
        if [[ "$status" == "0" ]]; then
            break
        fi
    done

    # Copy RDB file
    local rdb_path=$(redis_cli CONFIG GET dir | tail -1)/$(redis_cli CONFIG GET dbfilename | tail -1)

    if [[ -f "$rdb_path" ]]; then
        cp "$rdb_path" "$backup_file"
        gzip -9 "$backup_file"
        backup_file="${backup_file}.gz"

        local size=$(du -h "$backup_file" | cut -f1)
        log_info "Backup created: $backup_file ($size)"

        # Upload to cloud
        upload_to_cloud "$backup_file" "redis/$(basename "$backup_file")"

        # Cleanup
        cleanup_old_backups

        send_notification "success" "Redis backup completed: $size, $db_size keys"
    else
        log_error "RDB file not found: $rdb_path"
        send_notification "error" "Redis backup failed - RDB file not found"
        exit 1
    fi

    log_info "=========================================="
    log_info "Redis backup completed"
    log_info "=========================================="
}

main "$@"
