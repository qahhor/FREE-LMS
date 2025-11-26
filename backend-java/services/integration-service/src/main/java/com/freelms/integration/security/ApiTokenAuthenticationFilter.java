package com.freelms.integration.security;

import com.freelms.integration.token.ApiToken;
import com.freelms.integration.token.ApiTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Smartup LMS - API Token Authentication Filter
 *
 * Authenticates API requests using bearer tokens.
 * Validates token, checks scopes, and sets security context.
 */
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiTokenAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String API_KEY_HEADER = "X-API-Key";

    private final ApiTokenService apiTokenService;

    public ApiTokenAuthenticationFilter(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip authentication for health checks and public endpoints
        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token from request
        String token = extractToken(request);

        if (token == null) {
            log.debug("No API token found in request to: {}", path);
            sendUnauthorizedResponse(response, "Missing API token");
            return;
        }

        // Validate token
        String clientIp = getClientIp(request);
        Optional<ApiToken> validatedToken = apiTokenService.validateToken(token, clientIp);

        if (validatedToken.isEmpty()) {
            log.warn("Invalid API token used from IP: {}", clientIp);
            sendUnauthorizedResponse(response, "Invalid or expired API token");
            return;
        }

        ApiToken apiToken = validatedToken.get();

        // Check required scope for endpoint
        String requiredScope = getRequiredScope(request.getMethod(), path);
        if (requiredScope != null && !apiToken.hasScope(requiredScope)) {
            log.warn("Token {} lacks required scope: {}", apiToken.getName(), requiredScope);
            sendForbiddenResponse(response, "Insufficient permissions. Required scope: " + requiredScope);
            return;
        }

        // Create authentication with token details
        ApiTokenPrincipal principal = new ApiTokenPrincipal(apiToken);
        List<SimpleGrantedAuthority> authorities = apiToken.getScopes().stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .collect(Collectors.toList());

        // Add role-based authority
        authorities.add(new SimpleGrantedAuthority("ROLE_API_" + apiToken.getType().name()));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Authenticated API request from token: {} to: {}", apiToken.getName(), path);
        filterChain.doFilter(request, response);
    }

    /**
     * Extract token from Authorization header or X-API-Key header
     */
    private String extractToken(HttpServletRequest request) {
        // Check Authorization header first
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        // Check X-API-Key header
        String apiKeyHeader = request.getHeader(API_KEY_HEADER);
        if (apiKeyHeader != null && !apiKeyHeader.isEmpty()) {
            return apiKeyHeader;
        }

        // Check query parameter (not recommended, but supported)
        return request.getParameter("api_key");
    }

    /**
     * Determine required scope based on HTTP method and path
     */
    private String getRequiredScope(String method, String path) {
        // Users endpoints
        if (path.contains("/api/v1/users")) {
            return isReadMethod(method) ? ApiToken.Scopes.USERS_READ : ApiToken.Scopes.USERS_WRITE;
        }

        // Courses endpoints
        if (path.contains("/api/v1/courses")) {
            return isReadMethod(method) ? ApiToken.Scopes.COURSES_READ : ApiToken.Scopes.COURSES_WRITE;
        }

        // Enrollments endpoints
        if (path.contains("/api/v1/enrollments")) {
            return isReadMethod(method) ? ApiToken.Scopes.ENROLLMENTS_READ : ApiToken.Scopes.ENROLLMENTS_WRITE;
        }

        // Progress endpoints
        if (path.contains("/api/v1/progress")) {
            return isReadMethod(method) ? ApiToken.Scopes.PROGRESS_READ : ApiToken.Scopes.PROGRESS_WRITE;
        }

        // Reports endpoints
        if (path.contains("/api/v1/reports")) {
            return ApiToken.Scopes.REPORTS_READ;
        }

        // Webhooks endpoints
        if (path.contains("/api/v1/webhooks")) {
            return ApiToken.Scopes.WEBHOOKS_MANAGE;
        }

        // Organization endpoints
        if (path.contains("/api/v1/organization")) {
            return isReadMethod(method) ? ApiToken.Scopes.ORGANIZATION_READ : ApiToken.Scopes.ORGANIZATION_WRITE;
        }

        return null; // No specific scope required
    }

    private boolean isReadMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/health") ||
               path.equals("/ready") ||
               path.equals("/metrics") ||
               path.startsWith("/api/v1/public/") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"status\":401}", message));
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\":\"Forbidden\",\"message\":\"%s\",\"status\":403}", message));
    }
}
