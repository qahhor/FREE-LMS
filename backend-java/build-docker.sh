#!/bin/bash

# =============================================================================
# FREE LMS - Docker Build Script
# Builds Docker images for all 32 microservices
# =============================================================================

set -e

echo "=============================================="
echo "  FREE LMS - Docker Build"
echo "  Building 32 microservice images..."
echo "=============================================="

# Configuration
REGISTRY="${DOCKER_REGISTRY:-freelms}"
VERSION="${BUILD_VERSION:-1.0.0}"
PUSH="${PUSH_IMAGES:-false}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Services list
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

# Build JAR files first
echo "Step 1: Building JAR files..."
mvn clean package -DskipTests -T 4

# Build Docker images
echo ""
echo "Step 2: Building Docker images..."
echo ""

SUCCESS_COUNT=0
FAIL_COUNT=0

for service in "${SERVICES[@]}"; do
    jar_file="services/$service/target/$service-1.0.0-SNAPSHOT.jar"
    dockerfile="services/$service/Dockerfile"

    if [ ! -f "$jar_file" ]; then
        echo -e "${RED}✗ $service - JAR not found${NC}"
        ((FAIL_COUNT++))
        continue
    fi

    # Create Dockerfile if not exists
    if [ ! -f "$dockerfile" ]; then
        cat > "$dockerfile" << 'DOCKERFILE'
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="FREE LMS Team"

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy JAR file
COPY target/*.jar app.jar

# Set ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
DOCKERFILE
    fi

    # Build Docker image
    image_name="$REGISTRY/$service:$VERSION"

    echo -e "${YELLOW}Building: $image_name${NC}"

    if docker build -t "$image_name" -f "$dockerfile" "services/$service" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ $image_name${NC}"
        ((SUCCESS_COUNT++))

        # Push if requested
        if [ "$PUSH" = "true" ]; then
            docker push "$image_name"
        fi
    else
        echo -e "${RED}✗ $image_name - build failed${NC}"
        ((FAIL_COUNT++))
    fi
done

echo ""
echo "=============================================="
echo "  Docker Build Complete!"
echo "=============================================="
echo ""
echo "Success: $SUCCESS_COUNT images"
echo "Failed: $FAIL_COUNT images"
echo ""
echo "To push images to registry:"
echo "  PUSH_IMAGES=true ./build-docker.sh"
echo ""
echo "To use custom registry:"
echo "  DOCKER_REGISTRY=myregistry.io ./build-docker.sh"
echo ""
