#!/bin/bash

# =============================================================================
# FREE LMS - Production Build Script
# Builds all 32 microservices as executable JAR files
# =============================================================================

set -e

echo "=============================================="
echo "  FREE LMS - Production Build"
echo "  Building 32 microservices..."
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Build directory
BUILD_DIR="$(pwd)/target/production"
mkdir -p "$BUILD_DIR"

# Start time
START_TIME=$(date +%s)

# Build function
build_service() {
    local service=$1
    echo -e "${YELLOW}Building: $service${NC}"

    if mvn clean package -DskipTests -pl "services/$service" -am -q; then
        # Copy JAR to production directory
        local jar_file="services/$service/target/$service-1.0.0-SNAPSHOT.jar"
        if [ -f "$jar_file" ]; then
            cp "$jar_file" "$BUILD_DIR/"
            echo -e "${GREEN}✓ $service${NC}"
            return 0
        fi
    fi

    echo -e "${RED}✗ $service - FAILED${NC}"
    return 1
}

# Option 1: Build all at once (faster with parallel threads)
echo ""
echo "Starting parallel build with 4 threads..."
echo ""

mvn clean package -DskipTests -T 4

# Collect all JARs
echo ""
echo "Collecting JAR files..."
echo ""

SERVICES=(
    "api-gateway"
    "discovery-server"
    "config-server"
    "user-service"
    "course-service"
    "content-service"
    "assessment-service"
    "enrollment-service"
    "notification-service"
    "payment-service"
    "analytics-service"
    "certificate-service"
    "forum-service"
    "calendar-service"
    "messaging-service"
    "reporting-service"
    "integration-service"
    "file-storage-service"
    "gamification-service"
    "social-learning-service"
    "mobile-api-service"
    "admin-service"
    "search-service"
    "media-processing-service"
    "event-service"
    "authoring-service"
    "proctoring-service"
    "assignment-review-service"
    "resource-booking-service"
    "audit-logging-service"
    "lti-service"
    "bot-platform-service"
)

SUCCESS_COUNT=0
FAIL_COUNT=0

for service in "${SERVICES[@]}"; do
    jar_file="services/$service/target/$service-1.0.0-SNAPSHOT.jar"
    if [ -f "$jar_file" ]; then
        cp "$jar_file" "$BUILD_DIR/"
        echo -e "${GREEN}✓ $service.jar${NC}"
        ((SUCCESS_COUNT++))
    else
        echo -e "${RED}✗ $service.jar - not found${NC}"
        ((FAIL_COUNT++))
    fi
done

# End time
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo "=============================================="
echo "  Build Complete!"
echo "=============================================="
echo ""
echo "Duration: ${DURATION}s"
echo "Success: $SUCCESS_COUNT services"
echo "Failed: $FAIL_COUNT services"
echo ""
echo "JAR files location: $BUILD_DIR"
echo ""

# List built JARs
echo "Built artifacts:"
ls -lh "$BUILD_DIR"/*.jar 2>/dev/null || echo "No JAR files found"

echo ""
echo "To run a service:"
echo "  java -jar $BUILD_DIR/<service-name>-1.0.0-SNAPSHOT.jar"
echo ""
