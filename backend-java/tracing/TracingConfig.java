package com.freelms.common.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * FREE LMS - Distributed Tracing Configuration
 *
 * Configures OpenTelemetry for distributed tracing across all microservices.
 * Integrates with Jaeger for trace visualization and analysis.
 *
 * Features:
 * - Automatic span creation for HTTP requests
 * - Database query tracing
 * - Kafka message tracing
 * - Custom span attributes for business context
 *
 * @author FREE LMS Team
 */
@Configuration
public class TracingConfig {

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Value("${otel.exporter.otlp.endpoint:http://localhost:4317}")
    private String otlpEndpoint;

    @Value("${otel.traces.sampler.probability:1.0}")
    private double samplerProbability;

    /**
     * Creates OpenTelemetry SDK with OTLP exporter
     */
    @Bean
    public OpenTelemetry openTelemetry() {
        // Create resource with service information
        Resource resource = Resource.getDefault()
            .merge(Resource.create(Attributes.of(
                ResourceAttributes.SERVICE_NAME, serviceName,
                ResourceAttributes.SERVICE_NAMESPACE, "freelms",
                ResourceAttributes.DEPLOYMENT_ENVIRONMENT,
                    System.getenv().getOrDefault("ENVIRONMENT", "development")
            )));

        // Configure OTLP exporter
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(otlpEndpoint)
            .setTimeout(30, TimeUnit.SECONDS)
            .build();

        // Configure tracer provider with batch processor
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter)
                .setMaxQueueSize(2048)
                .setMaxExportBatchSize(512)
                .setScheduleDelay(5, TimeUnit.SECONDS)
                .build())
            .setResource(resource)
            .build();

        // Build OpenTelemetry SDK
        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();
    }
}

/**
 * Tracing utility for creating custom spans
 */
class TracingUtils {

    private final Tracer tracer;

    public TracingUtils(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Creates a new span for a business operation
     */
    public Span startSpan(String operationName) {
        return tracer.nextSpan().name(operationName).start();
    }

    /**
     * Adds user context to current span
     */
    public void addUserContext(Span span, String userId, String organizationId) {
        span.tag("user.id", maskSensitiveData(userId));
        span.tag("organization.id", organizationId);
    }

    /**
     * Adds business context to current span
     */
    public void addBusinessContext(Span span, String courseId, String action) {
        span.tag("course.id", courseId);
        span.tag("business.action", action);
    }

    /**
     * Records an error on the span
     */
    public void recordError(Span span, Throwable error) {
        span.tag("error", "true");
        span.tag("error.message", error.getMessage());
        span.tag("error.type", error.getClass().getSimpleName());
    }

    /**
     * Masks sensitive data for security
     */
    private String maskSensitiveData(String data) {
        if (data == null || data.length() < 8) {
            return "***";
        }
        return data.substring(0, 4) + "****" + data.substring(data.length() - 4);
    }
}
