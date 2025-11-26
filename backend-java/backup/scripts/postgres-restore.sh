#!/bin/bash
# ============================================================================
# Smartup LMS - PostgreSQL Restore Script
# Restore database from backup
# ============================================================================

set -euo pipefail

# =============================================================================
# Configuration
# =============================================================================

DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
DB_USER="${POSTGRES_USER:-freelms}"
DB_PASSWORD="${POSTGRES_PASSWORD}"
DB_NAME="${POSTGRES_DB:-freelms}"

LOG_FILE="/var/log/freelms/restore-postgres.log"

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

usage() {
    cat << EOF
Usage: $(basename "$0") [OPTIONS] <backup_file>

Restore Smartup LMS PostgreSQL database from backup file.

OPTIONS:
    -h, --help              Show this help message
    -t, --target-db NAME    Target database name (default: $DB_NAME)
    -n, --no-confirm        Skip confirmation prompt
    --drop-existing         Drop existing database before restore
    --verify-only           Verify backup file without restoring

EXAMPLES:
    $(basename "$0") /var/backups/freelms/postgres/daily/freelms_daily_20231201_120000.sql.gz
    $(basename "$0") --drop-existing backup.sql.gz
    $(basename "$0") --verify-only backup.sql.gz

EOF
    exit 0
}

decompress_file() {
    local file="$1"
    local output_file="${file%.*}"

    case "$file" in
        *.gz) gunzip -c "$file" > "$output_file" ;;
        *.lz4) lz4 -d "$file" "$output_file" ;;
        *.zst) zstd -d "$file" -o "$output_file" ;;
        *) output_file="$file" ;;
    esac

    echo "$output_file"
}

verify_backup() {
    local file="$1"

    log_info "Verifying backup file: $file"

    # Check file exists
    if [[ ! -f "$file" ]]; then
        log_error "Backup file not found: $file"
        return 1
    fi

    # Check compression integrity
    case "$file" in
        *.gz) gzip -t "$file" 2>/dev/null || { log_error "Gzip integrity check failed"; return 1; } ;;
        *.lz4) lz4 -t "$file" 2>/dev/null || { log_error "LZ4 integrity check failed"; return 1; } ;;
        *.zst) zstd -t "$file" 2>/dev/null || { log_error "Zstd integrity check failed"; return 1; } ;;
    esac

    # Check SQL structure
    local temp_file=$(mktemp)
    case "$file" in
        *.gz) gunzip -c "$file" | head -100 > "$temp_file" ;;
        *.lz4) lz4 -d "$file" -c | head -100 > "$temp_file" ;;
        *.zst) zstd -d "$file" -c | head -100 > "$temp_file" ;;
        *) head -100 "$file" > "$temp_file" ;;
    esac

    if grep -q "PostgreSQL database dump" "$temp_file"; then
        log_info "Backup file verified successfully"
        rm "$temp_file"
        return 0
    else
        log_error "File does not appear to be a valid PostgreSQL dump"
        rm "$temp_file"
        return 1
    fi
}

# =============================================================================
# Main
# =============================================================================

main() {
    local backup_file=""
    local target_db="$DB_NAME"
    local no_confirm=false
    local drop_existing=false
    local verify_only=false

    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case "$1" in
            -h|--help) usage ;;
            -t|--target-db) target_db="$2"; shift 2 ;;
            -n|--no-confirm) no_confirm=true; shift ;;
            --drop-existing) drop_existing=true; shift ;;
            --verify-only) verify_only=true; shift ;;
            -*) log_error "Unknown option: $1"; usage ;;
            *) backup_file="$1"; shift ;;
        esac
    done

    if [[ -z "$backup_file" ]]; then
        log_error "Backup file not specified"
        usage
    fi

    mkdir -p "$(dirname "$LOG_FILE")"

    log_info "=========================================="
    log_info "Smartup LMS PostgreSQL Restore"
    log_info "=========================================="
    log_info "Backup file: $backup_file"
    log_info "Target database: $target_db"

    # Verify backup
    verify_backup "$backup_file" || exit 1

    if [[ "$verify_only" == "true" ]]; then
        log_info "Verification complete (--verify-only mode)"
        exit 0
    fi

    # Confirmation
    if [[ "$no_confirm" != "true" ]]; then
        echo ""
        echo "WARNING: This will restore data to database '$target_db'"
        [[ "$drop_existing" == "true" ]] && echo "         The existing database will be DROPPED!"
        echo ""
        read -p "Are you sure you want to continue? (yes/no): " confirm
        if [[ "$confirm" != "yes" ]]; then
            log_info "Restore cancelled by user"
            exit 0
        fi
    fi

    export PGPASSWORD="$DB_PASSWORD"

    # Drop existing database if requested
    if [[ "$drop_existing" == "true" ]]; then
        log_warn "Dropping existing database: $target_db"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres \
            -c "DROP DATABASE IF EXISTS $target_db;" 2>/dev/null || true
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres \
            -c "CREATE DATABASE $target_db;" 2>/dev/null || true
    fi

    # Decompress if needed
    log_info "Preparing backup file..."
    local sql_file=$(decompress_file "$backup_file")
    local temp_created=false
    [[ "$sql_file" != "$backup_file" ]] && temp_created=true

    # Perform restore
    log_info "Starting restore..."
    local start_time=$(date +%s)

    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$target_db" \
        -v ON_ERROR_STOP=1 \
        -f "$sql_file" 2>> "$LOG_FILE" || {
        log_error "Restore failed"
        [[ "$temp_created" == "true" ]] && rm -f "$sql_file"
        exit 1
    }

    local end_time=$(date +%s)
    local duration=$((end_time - start_time))

    # Cleanup
    [[ "$temp_created" == "true" ]] && rm -f "$sql_file"

    # Verify restore
    log_info "Verifying restored data..."
    local table_count=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$target_db" \
        -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")

    log_info "=========================================="
    log_info "Restore completed successfully"
    log_info "Duration: ${duration}s"
    log_info "Tables restored: $table_count"
    log_info "=========================================="
}

main "$@"
