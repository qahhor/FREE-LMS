#!/bin/bash
# ============================================================================
# Smartup LMS - PostgreSQL Backup Script
# Automated backup for production database
# Target: 200 clients, 100,000 users
# ============================================================================

set -euo pipefail

# =============================================================================
# Configuration
# =============================================================================

# Database connection
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
DB_USER="${POSTGRES_USER:-freelms}"
DB_PASSWORD="${POSTGRES_PASSWORD}"
DB_NAME="${POSTGRES_DB:-freelms}"

# Backup settings
BACKUP_DIR="${BACKUP_DIR:-/var/backups/freelms/postgres}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"
RETENTION_WEEKS="${RETENTION_WEEKS:-12}"
RETENTION_MONTHS="${RETENTION_MONTHS:-12}"

# Compression
COMPRESSION="${COMPRESSION:-gzip}"  # gzip, lz4, zstd, none

# Cloud upload settings (optional)
S3_BUCKET="${S3_BUCKET:-}"
S3_REGION="${S3_REGION:-us-east-1}"
GCS_BUCKET="${GCS_BUCKET:-}"

# Notification settings
SLACK_WEBHOOK="${SLACK_WEBHOOK:-}"
EMAIL_TO="${EMAIL_TO:-}"

# Logging
LOG_FILE="/var/log/freelms/backup-postgres.log"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
DATE_DAY=$(date +"%d")
DATE_WEEKDAY=$(date +"%u")  # 1=Monday, 7=Sunday

# =============================================================================
# Functions
# =============================================================================

log() {
    local level="$1"
    shift
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] [$level] $*" | tee -a "$LOG_FILE"
}

log_info() { log "INFO" "$@"; }
log_warn() { log "WARN" "$@"; }
log_error() { log "ERROR" "$@"; }

send_notification() {
    local status="$1"
    local message="$2"
    local details="${3:-}"

    # Slack notification
    if [[ -n "$SLACK_WEBHOOK" ]]; then
        local color="good"
        [[ "$status" == "error" ]] && color="danger"
        [[ "$status" == "warning" ]] && color="warning"

        curl -s -X POST "$SLACK_WEBHOOK" \
            -H 'Content-Type: application/json' \
            -d "{
                \"attachments\": [{
                    \"color\": \"$color\",
                    \"title\": \"Smartup LMS PostgreSQL Backup - ${status^^}\",
                    \"text\": \"$message\",
                    \"fields\": [{\"title\": \"Details\", \"value\": \"$details\", \"short\": false}],
                    \"footer\": \"Smartup LMS Backup System\",
                    \"ts\": $(date +%s)
                }]
            }" || true
    fi

    # Email notification
    if [[ -n "$EMAIL_TO" ]]; then
        echo -e "Subject: Smartup LMS Backup - ${status^^}\n\n$message\n\nDetails:\n$details" | \
            sendmail "$EMAIL_TO" || true
    fi
}

cleanup_old_backups() {
    log_info "Cleaning up old backups..."

    # Daily backups - keep for RETENTION_DAYS days
    find "$BACKUP_DIR/daily" -type f -name "*.sql*" -mtime +$RETENTION_DAYS -delete 2>/dev/null || true

    # Weekly backups - keep for RETENTION_WEEKS weeks
    find "$BACKUP_DIR/weekly" -type f -name "*.sql*" -mtime +$((RETENTION_WEEKS * 7)) -delete 2>/dev/null || true

    # Monthly backups - keep for RETENTION_MONTHS months
    find "$BACKUP_DIR/monthly" -type f -name "*.sql*" -mtime +$((RETENTION_MONTHS * 30)) -delete 2>/dev/null || true

    log_info "Cleanup completed"
}

get_compression_extension() {
    case "$COMPRESSION" in
        gzip) echo ".gz" ;;
        lz4) echo ".lz4" ;;
        zstd) echo ".zst" ;;
        none) echo "" ;;
        *) echo ".gz" ;;
    esac
}

compress_file() {
    local file="$1"

    case "$COMPRESSION" in
        gzip) gzip -9 "$file" ;;
        lz4) lz4 -9 "$file" && rm "$file" ;;
        zstd) zstd -19 --rm "$file" ;;
        none) : ;;
    esac
}

upload_to_cloud() {
    local file="$1"
    local remote_path="$2"

    # Upload to S3
    if [[ -n "$S3_BUCKET" ]]; then
        log_info "Uploading to S3: s3://$S3_BUCKET/$remote_path"
        aws s3 cp "$file" "s3://$S3_BUCKET/$remote_path" \
            --region "$S3_REGION" \
            --storage-class STANDARD_IA || {
            log_error "Failed to upload to S3"
            return 1
        }
    fi

    # Upload to GCS
    if [[ -n "$GCS_BUCKET" ]]; then
        log_info "Uploading to GCS: gs://$GCS_BUCKET/$remote_path"
        gsutil cp "$file" "gs://$GCS_BUCKET/$remote_path" || {
            log_error "Failed to upload to GCS"
            return 1
        }
    fi
}

perform_backup() {
    local backup_type="$1"
    local backup_subdir="$BACKUP_DIR/$backup_type"
    local ext=$(get_compression_extension)
    local backup_file="$backup_subdir/freelms_${backup_type}_${TIMESTAMP}.sql"
    local final_file="${backup_file}${ext}"

    mkdir -p "$backup_subdir"

    log_info "Starting $backup_type backup..."
    local start_time=$(date +%s)

    # Set password
    export PGPASSWORD="$DB_PASSWORD"

    # Perform backup with parallel jobs for large databases
    pg_dump \
        -h "$DB_HOST" \
        -p "$DB_PORT" \
        -U "$DB_USER" \
        -d "$DB_NAME" \
        -F plain \
        -v \
        --no-owner \
        --no-privileges \
        --if-exists \
        --clean \
        --create \
        -f "$backup_file" 2>> "$LOG_FILE" || {
        log_error "pg_dump failed"
        return 1
    }

    # Get uncompressed size
    local raw_size=$(du -h "$backup_file" | cut -f1)

    # Compress
    if [[ "$COMPRESSION" != "none" ]]; then
        log_info "Compressing backup with $COMPRESSION..."
        compress_file "$backup_file"
    fi

    # Get final size
    local final_size=$(du -h "$final_file" | cut -f1)

    local end_time=$(date +%s)
    local duration=$((end_time - start_time))

    log_info "Backup completed: $final_file"
    log_info "Size: $raw_size -> $final_size (compressed), Duration: ${duration}s"

    # Upload to cloud
    upload_to_cloud "$final_file" "postgres/$backup_type/$(basename "$final_file")" || true

    # Calculate checksum
    local checksum=$(sha256sum "$final_file" | cut -d' ' -f1)
    echo "$checksum  $(basename "$final_file")" >> "$backup_subdir/checksums.txt"

    echo "$final_file"
}

verify_backup() {
    local backup_file="$1"

    log_info "Verifying backup integrity..."

    # Test decompression
    local test_result
    case "$COMPRESSION" in
        gzip) test_result=$(gzip -t "$backup_file" 2>&1) ;;
        lz4) test_result=$(lz4 -t "$backup_file" 2>&1) ;;
        zstd) test_result=$(zstd -t "$backup_file" 2>&1) ;;
        none) test_result="OK" ;;
    esac

    if [[ $? -eq 0 ]]; then
        log_info "Backup verification passed"
        return 0
    else
        log_error "Backup verification failed: $test_result"
        return 1
    fi
}

# =============================================================================
# Main Execution
# =============================================================================

main() {
    log_info "=========================================="
    log_info "Smartup LMS PostgreSQL Backup Starting"
    log_info "=========================================="
    log_info "Database: $DB_HOST:$DB_PORT/$DB_NAME"
    log_info "Backup directory: $BACKUP_DIR"
    log_info "Compression: $COMPRESSION"

    # Create directories
    mkdir -p "$BACKUP_DIR"/{daily,weekly,monthly}
    mkdir -p "$(dirname "$LOG_FILE")"

    local backup_file=""
    local backup_status="success"
    local backup_details=""

    # Determine backup type based on day
    local backup_types=("daily")

    # Weekly backup on Sunday
    if [[ "$DATE_WEEKDAY" == "7" ]]; then
        backup_types+=("weekly")
    fi

    # Monthly backup on 1st of month
    if [[ "$DATE_DAY" == "01" ]]; then
        backup_types+=("monthly")
    fi

    for backup_type in "${backup_types[@]}"; do
        backup_file=$(perform_backup "$backup_type") || {
            backup_status="error"
            backup_details="Failed to create $backup_type backup"
            send_notification "$backup_status" "Backup failed" "$backup_details"
            exit 1
        }

        verify_backup "$backup_file" || {
            backup_status="warning"
            backup_details="Backup created but verification failed: $backup_file"
        }

        backup_details+="$backup_type: $(basename "$backup_file")\n"
    done

    # Cleanup old backups
    cleanup_old_backups

    # Send success notification
    send_notification "$backup_status" "PostgreSQL backup completed successfully" "$backup_details"

    log_info "=========================================="
    log_info "Backup process completed"
    log_info "=========================================="
}

# Run main function
main "$@"
