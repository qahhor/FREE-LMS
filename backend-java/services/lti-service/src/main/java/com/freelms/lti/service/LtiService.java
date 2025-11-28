package com.freelms.lti.service;

import com.freelms.lti.entity.LtiLaunch;
import com.freelms.lti.entity.LtiPlatform;
import com.freelms.lti.entity.LtiTool;
import com.freelms.lti.repository.LtiLaunchRepository;
import com.freelms.lti.repository.LtiPlatformRepository;
import com.freelms.lti.repository.LtiToolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Core LTI 1.3 service for platform and tool management.
 */
@Service
public class LtiService {

    private static final Logger log = LoggerFactory.getLogger(LtiService.class);

    private final LtiPlatformRepository platformRepository;
    private final LtiToolRepository toolRepository;
    private final LtiLaunchRepository launchRepository;

    public LtiService(LtiPlatformRepository platformRepository,
                      LtiToolRepository toolRepository,
                      LtiLaunchRepository launchRepository) {
        this.platformRepository = platformRepository;
        this.toolRepository = toolRepository;
        this.launchRepository = launchRepository;
    }

    // ==================== Platform Management ====================

    @Transactional
    public LtiPlatform registerPlatform(LtiPlatform platform) {
        // Generate deployment ID if not provided
        if (platform.getDeploymentId() == null) {
            platform.setDeploymentId(UUID.randomUUID().toString());
        }

        // Generate key pair for this platform
        if (platform.getKeyId() == null) {
            platform.setKeyId(UUID.randomUUID().toString());
        }

        return platformRepository.save(platform);
    }

    @Transactional
    public LtiPlatform updatePlatform(UUID platformId, LtiPlatform updates) {
        LtiPlatform platform = getPlatform(platformId);

        if (updates.getName() != null) platform.setName(updates.getName());
        if (updates.getDescription() != null) platform.setDescription(updates.getDescription());
        if (updates.getOidcAuthUrl() != null) platform.setOidcAuthUrl(updates.getOidcAuthUrl());
        if (updates.getAccessTokenUrl() != null) platform.setAccessTokenUrl(updates.getAccessTokenUrl());
        if (updates.getJwksUrl() != null) platform.setJwksUrl(updates.getJwksUrl());
        if (updates.getSupportsAgs() != null) platform.setSupportsAgs(updates.getSupportsAgs());
        if (updates.getSupportsNrps() != null) platform.setSupportsNrps(updates.getSupportsNrps());
        if (updates.getSupportsDeepLinking() != null) platform.setSupportsDeepLinking(updates.getSupportsDeepLinking());

        return platformRepository.save(platform);
    }

    public LtiPlatform getPlatform(UUID platformId) {
        return platformRepository.findById(platformId)
                .orElseThrow(() -> new RuntimeException("Platform not found: " + platformId));
    }

    public LtiPlatform getPlatformByIssuer(String issuer) {
        return platformRepository.findByIssuer(issuer)
                .orElseThrow(() -> new RuntimeException("Platform not found for issuer: " + issuer));
    }

    public LtiPlatform getPlatformByIssuerAndClientId(String issuer, String clientId) {
        return platformRepository.findByIssuerAndClientId(issuer, clientId)
                .orElseThrow(() -> new RuntimeException("Platform not found"));
    }

    public List<LtiPlatform> getPlatforms(Long organizationId) {
        if (organizationId != null) {
            return platformRepository.findByOrganizationId(organizationId);
        }
        return platformRepository.findAll();
    }

    @Transactional
    public void updatePlatformStatus(UUID platformId, LtiPlatform.PlatformStatus status) {
        LtiPlatform platform = getPlatform(platformId);
        platform.setStatus(status);
        platformRepository.save(platform);
    }

    // ==================== Tool Management ====================

    @Transactional
    public LtiTool registerTool(LtiTool tool) {
        if (tool.getClientId() == null) {
            tool.setClientId(UUID.randomUUID().toString());
        }
        return toolRepository.save(tool);
    }

    @Transactional
    public LtiTool updateTool(UUID toolId, LtiTool updates) {
        LtiTool tool = getTool(toolId);

        if (updates.getName() != null) tool.setName(updates.getName());
        if (updates.getDescription() != null) tool.setDescription(updates.getDescription());
        if (updates.getTargetLinkUri() != null) tool.setTargetLinkUri(updates.getTargetLinkUri());
        if (updates.getOidcInitiationUrl() != null) tool.setOidcInitiationUrl(updates.getOidcInitiationUrl());
        if (updates.getDeepLinkingUrl() != null) tool.setDeepLinkingUrl(updates.getDeepLinkingUrl());
        if (updates.getPrivacyLevel() != null) tool.setPrivacyLevel(updates.getPrivacyLevel());
        if (updates.getPlacement() != null) tool.setPlacement(updates.getPlacement());
        if (updates.getCustomParameters() != null) tool.setCustomParameters(updates.getCustomParameters());

        return toolRepository.save(tool);
    }

    public LtiTool getTool(UUID toolId) {
        return toolRepository.findById(toolId)
                .orElseThrow(() -> new RuntimeException("Tool not found: " + toolId));
    }

    public LtiTool getToolByClientId(String clientId) {
        return toolRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Tool not found for client ID: " + clientId));
    }

    public List<LtiTool> getTools(Long organizationId) {
        if (organizationId != null) {
            return toolRepository.findByOrganizationId(organizationId);
        }
        return toolRepository.findAll();
    }

    public List<LtiTool> getActiveTools(Long organizationId) {
        return toolRepository.findByOrganizationIdAndStatus(organizationId, LtiTool.ToolStatus.ACTIVE);
    }

    @Transactional
    public void updateToolStatus(UUID toolId, LtiTool.ToolStatus status) {
        LtiTool tool = getTool(toolId);
        tool.setStatus(status);
        toolRepository.save(tool);
    }

    // ==================== Launch Session Management ====================

    @Transactional
    public LtiLaunch createLaunchSession(UUID platformId, UUID toolId, String ipAddress, String userAgent) {
        LtiLaunch launch = new LtiLaunch();
        launch.setNonce(UUID.randomUUID().toString());
        launch.setState(UUID.randomUUID().toString());
        launch.setPlatformId(platformId);
        launch.setToolId(toolId);
        launch.setStatus(LtiLaunch.LaunchStatus.INITIATED);
        launch.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        launch.setIpAddress(ipAddress);
        launch.setUserAgent(userAgent);

        return launchRepository.save(launch);
    }

    @Transactional
    public LtiLaunch updateLaunchStatus(String state, LtiLaunch.LaunchStatus status) {
        LtiLaunch launch = launchRepository.findByState(state)
                .orElseThrow(() -> new RuntimeException("Launch session not found"));

        launch.setStatus(status);
        if (status == LtiLaunch.LaunchStatus.COMPLETED) {
            launch.setCompletedAt(LocalDateTime.now());
        }

        return launchRepository.save(launch);
    }

    @Transactional
    public void updateLaunchWithClaims(String state, Map<String, Object> claims) {
        LtiLaunch launch = launchRepository.findByState(state)
                .orElseThrow(() -> new RuntimeException("Launch session not found"));

        // Extract user info
        if (claims.containsKey("sub")) {
            launch.setLtiUserId((String) claims.get("sub"));
        }
        if (claims.containsKey("name")) {
            launch.setUserName((String) claims.get("name"));
        }
        if (claims.containsKey("email")) {
            launch.setUserEmail((String) claims.get("email"));
        }

        // Extract context info
        Map<String, Object> context = (Map<String, Object>) claims.get("https://purl.imsglobal.org/spec/lti/claim/context");
        if (context != null) {
            launch.setLtiContextId((String) context.get("id"));
            launch.setContextTitle((String) context.get("title"));
        }

        // Extract resource link
        Map<String, Object> resourceLink = (Map<String, Object>) claims.get("https://purl.imsglobal.org/spec/lti/claim/resource_link");
        if (resourceLink != null) {
            launch.setResourceLinkId((String) resourceLink.get("id"));
            launch.setResourceLinkTitle((String) resourceLink.get("title"));
        }

        // Extract roles
        List<String> roles = (List<String>) claims.get("https://purl.imsglobal.org/spec/lti/claim/roles");
        if (roles != null) {
            launch.setRoles(String.join(",", roles));
        }

        // Extract AGS endpoints
        Map<String, Object> ags = (Map<String, Object>) claims.get("https://purl.imsglobal.org/spec/lti-ags/claim/endpoint");
        if (ags != null) {
            launch.setLineItemsUrl((String) ags.get("lineitems"));
            launch.setLineItemUrl((String) ags.get("lineitem"));
            List<String> scopes = (List<String>) ags.get("scope");
            if (scopes != null && scopes.stream().anyMatch(s -> s.contains("score"))) {
                launch.setScoresUrl((String) ags.get("lineitem") + "/scores");
            }
        }

        // Extract NRPS endpoint
        Map<String, Object> nrps = (Map<String, Object>) claims.get("https://purl.imsglobal.org/spec/lti-nrps/claim/namesroleservice");
        if (nrps != null) {
            launch.setMembershipsUrl((String) nrps.get("context_memberships_url"));
        }

        // Extract deep linking
        Map<String, Object> dl = (Map<String, Object>) claims.get("https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings");
        if (dl != null) {
            launch.setDeepLinkReturnUrl((String) dl.get("deep_link_return_url"));
            launch.setDeepLinkData((String) dl.get("data"));
            launch.setMessageType(LtiLaunch.MessageType.DEEP_LINKING_REQUEST);
        }

        launch.setStatus(LtiLaunch.LaunchStatus.TOKEN_VALIDATED);
        launchRepository.save(launch);
    }

    public LtiLaunch getLaunchByState(String state) {
        return launchRepository.findByState(state)
                .orElseThrow(() -> new RuntimeException("Launch session not found"));
    }

    public LtiLaunch getLaunchByNonce(String nonce) {
        return launchRepository.findByNonce(nonce)
                .orElseThrow(() -> new RuntimeException("Launch session not found"));
    }

    @Transactional
    public void recordSuccessfulLaunch(UUID platformId) {
        LtiPlatform platform = getPlatform(platformId);
        platform.setLastLaunchAt(LocalDateTime.now());
        platform.setLaunchCount(platform.getLaunchCount() + 1);
        platformRepository.save(platform);
    }

    @Transactional
    public void recordToolLaunch(UUID toolId) {
        LtiTool tool = getTool(toolId);
        tool.setLastLaunchAt(LocalDateTime.now());
        tool.setLaunchCount(tool.getLaunchCount() + 1);
        toolRepository.save(tool);
    }

    // ==================== Statistics ====================

    public Map<String, Object> getPlatformStats(UUID platformId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> stats = new HashMap<>();

        LtiPlatform platform = getPlatform(platformId);
        stats.put("platformId", platformId);
        stats.put("name", platform.getName());
        stats.put("totalLaunches", platform.getLaunchCount());
        stats.put("lastLaunch", platform.getLastLaunchAt());

        Long recentLaunches = launchRepository.countSuccessfulLaunches(platformId, since);
        stats.put("recentLaunches", recentLaunches);
        stats.put("period", Map.of("days", days, "since", since));

        return stats;
    }

    // ==================== Maintenance ====================

    @Transactional
    public int cleanupExpiredLaunches() {
        return launchRepository.expireStaleLaunches(LocalDateTime.now());
    }
}
