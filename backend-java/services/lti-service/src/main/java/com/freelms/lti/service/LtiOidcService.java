package com.freelms.lti.service;

import com.freelms.lti.entity.LtiLaunch;
import com.freelms.lti.entity.LtiPlatform;
import com.freelms.lti.entity.LtiTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * LTI 1.3 OIDC (OpenID Connect) authentication service.
 */
@Service
public class LtiOidcService {

    private static final Logger log = LoggerFactory.getLogger(LtiOidcService.class);

    private final LtiService ltiService;

    @Value("${lti.base-url:http://localhost:8080}")
    private String baseUrl;

    public LtiOidcService(LtiService ltiService) {
        this.ltiService = ltiService;
    }

    /**
     * Handle OIDC login initiation (step 1 of LTI 1.3 launch).
     * This is called when a platform initiates a tool launch.
     */
    public String handleOidcInitiation(
            String iss,
            String loginHint,
            String targetLinkUri,
            String ltiMessageHint,
            String clientId,
            String deploymentId,
            String ipAddress,
            String userAgent) {

        log.info("OIDC initiation from issuer: {}, client: {}", iss, clientId);

        // Find the platform
        LtiPlatform platform;
        if (clientId != null) {
            platform = ltiService.getPlatformByIssuerAndClientId(iss, clientId);
        } else {
            platform = ltiService.getPlatformByIssuer(iss);
        }

        // Create launch session
        LtiLaunch launch = ltiService.createLaunchSession(platform.getId(), null, ipAddress, userAgent);
        launch.setStatus(LtiLaunch.LaunchStatus.OIDC_INITIATED);

        // Build authentication request to send back to platform
        String authUrl = UriComponentsBuilder.fromHttpUrl(platform.getOidcAuthUrl())
                .queryParam("scope", "openid")
                .queryParam("response_type", "id_token")
                .queryParam("response_mode", "form_post")
                .queryParam("client_id", platform.getClientId())
                .queryParam("redirect_uri", baseUrl + "/api/v1/lti/callback")
                .queryParam("state", launch.getState())
                .queryParam("nonce", launch.getNonce())
                .queryParam("login_hint", loginHint)
                .queryParam("prompt", "none")
                .build()
                .toUriString();

        if (ltiMessageHint != null) {
            authUrl += "&lti_message_hint=" + URLEncoder.encode(ltiMessageHint, StandardCharsets.UTF_8);
        }

        log.debug("Redirecting to platform auth URL: {}", authUrl);
        return authUrl;
    }

    /**
     * Generate OIDC initiation URL for launching a tool from our platform.
     */
    public String generateToolLaunchUrl(
            UUID toolId,
            Long userId,
            Long courseId,
            String resourceLinkId,
            Map<String, String> customParams) {

        LtiTool tool = ltiService.getTool(toolId);

        // Create launch session
        LtiLaunch launch = ltiService.createLaunchSession(null, toolId, null, null);
        launch.setUserId(userId);
        launch.setCourseId(courseId);
        launch.setResourceLinkId(resourceLinkId);

        // Build initiation URL
        String initUrl = UriComponentsBuilder.fromHttpUrl(tool.getOidcInitiationUrl())
                .queryParam("iss", baseUrl)
                .queryParam("login_hint", userId.toString())
                .queryParam("target_link_uri", tool.getTargetLinkUri())
                .queryParam("client_id", tool.getClientId())
                .queryParam("lti_deployment_id", "1")
                .build()
                .toUriString();

        log.debug("Generated tool launch URL: {}", initUrl);
        return initUrl;
    }

    /**
     * Get the redirect URI for our platform.
     */
    public String getRedirectUri() {
        return baseUrl + "/api/v1/lti/callback";
    }

    /**
     * Get the JWKS URL for our platform.
     */
    public String getJwksUrl() {
        return baseUrl + "/api/v1/lti/.well-known/jwks.json";
    }

    /**
     * Get platform configuration for tool registration.
     */
    public Map<String, Object> getPlatformConfiguration() {
        return Map.of(
                "issuer", baseUrl,
                "authorization_endpoint", baseUrl + "/api/v1/lti/authorize",
                "token_endpoint", baseUrl + "/api/v1/lti/token",
                "jwks_uri", getJwksUrl(),
                "registration_endpoint", baseUrl + "/api/v1/lti/register",
                "scopes_supported", new String[]{
                        "openid",
                        "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem",
                        "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly",
                        "https://purl.imsglobal.org/spec/lti-ags/scope/score",
                        "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly"
                },
                "response_types_supported", new String[]{"id_token"},
                "subject_types_supported", new String[]{"public"},
                "id_token_signing_alg_values_supported", new String[]{"RS256"},
                "claims_supported", new String[]{
                        "sub", "iss", "name", "given_name", "family_name", "email"
                },
                "https://purl.imsglobal.org/spec/lti-platform-configuration", Map.of(
                        "product_family_code", "free-lms",
                        "version", "1.0.0",
                        "messages_supported", new Object[]{
                                Map.of("type", "LtiResourceLinkRequest"),
                                Map.of("type", "LtiDeepLinkingRequest")
                        },
                        "variables", new String[]{
                                "CourseSection.sourcedId",
                                "Membership.role",
                                "Person.sourcedId",
                                "User.id"
                        }
                )
        );
    }
}
