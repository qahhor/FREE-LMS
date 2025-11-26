# =====================================================
# Smartup LMS - Multi-stage Docker Build
# Optimized for production deployment
# =====================================================

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom files first for layer caching
COPY pom.xml .
COPY common/pom.xml common/
COPY services/*/pom.xml services/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY . .

# Build all modules
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: Run as non-root user
RUN addgroup -g 1001 -S freelms && \
    adduser -u 1001 -S freelms -G freelms

# Install useful tools
RUN apk add --no-cache curl tzdata

# Set timezone
ENV TZ=UTC

WORKDIR /app

# Copy JAR from builder
ARG SERVICE_NAME
COPY --from=builder /app/services/${SERVICE_NAME}/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Switch to non-root user
USER freelms

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=production"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
