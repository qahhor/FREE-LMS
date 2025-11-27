FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install FFmpeg and ImageMagick for media processing
RUN apk add --no-cache ffmpeg imagemagick

RUN addgroup -S spring && adduser -S spring -G spring

# Create temp directory for processing
RUN mkdir -p /tmp/media-processing && chown spring:spring /tmp/media-processing

USER spring:spring
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8101
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8101/actuator/health || exit 1
ENTRYPOINT ["java", "-Xmx4g", "-jar", "app.jar"]
