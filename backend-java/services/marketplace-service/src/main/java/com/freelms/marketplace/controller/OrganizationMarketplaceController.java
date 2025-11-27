package com.freelms.marketplace.controller;

import com.freelms.marketplace.dto.*;
import com.freelms.marketplace.entity.*;
import com.freelms.marketplace.plugin.PluginManager;
import com.freelms.marketplace.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Smartup LMS - Organization Marketplace Controller
 *
 * Manages installed modules and subscribed courses for an organization.
 */
@RestController
@RequestMapping("/api/v1/organizations/{orgId}/marketplace")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organization Marketplace", description = "Manage installed modules and subscribed courses")
public class OrganizationMarketplaceController {

    private final ModuleInstallationRepository moduleInstallationRepository;
    private final CourseSubscriptionRepository courseSubscriptionRepository;
    private final MarketplaceModuleRepository moduleRepository;
    private final MarketplaceCourseRepository courseRepository;
    private final PluginManager pluginManager;

    // ==================== Dashboard ====================

    @GetMapping
    @Operation(summary = "Get organization marketplace dashboard")
    public ResponseEntity<OrganizationMarketplaceDto> getDashboard(@PathVariable Long orgId) {
        List<ModuleInstallationDto> installedModules = getInstalledModulesList(orgId);
        List<CourseSubscriptionDto> subscribedCourses = getSubscribedCoursesList(orgId);

        List<ModuleInstallationDto> updateAvailable = installedModules.stream()
                .filter(m -> m.isUpdateAvailable())
                .collect(Collectors.toList());

        OrganizationUsageStatsDto stats = OrganizationUsageStatsDto.builder()
                .totalInstalledModules(installedModules.size())
                .activeModules((int) installedModules.stream().filter(ModuleInstallationDto::isActive).count())
                .totalSubscribedCourses(subscribedCourses.size())
                .activeCourses((int) subscribedCourses.stream()
                        .filter(c -> c.getStatus() == CourseSubscription.SubscriptionStatus.ACTIVE).count())
                .totalCourseEnrollments(subscribedCourses.stream()
                        .mapToLong(c -> c.getCurrentEnrollments() != null ? c.getCurrentEnrollments() : 0).sum())
                .totalModuleUsage(installedModules.stream()
                        .mapToLong(m -> m.getUsageCount() != null ? m.getUsageCount() : 0).sum())
                .build();

        return ResponseEntity.ok(OrganizationMarketplaceDto.builder()
                .installedModules(installedModules)
                .subscribedCourses(subscribedCourses)
                .updateAvailable(updateAvailable)
                .usageStats(stats)
                .build());
    }

    // ==================== Module Installation ====================

    @GetMapping("/modules")
    @Operation(summary = "Get installed modules")
    public ResponseEntity<List<ModuleInstallationDto>> getInstalledModules(@PathVariable Long orgId) {
        return ResponseEntity.ok(getInstalledModulesList(orgId));
    }

    @PostMapping("/modules/install")
    @Operation(summary = "Install a module")
    @Transactional
    public ResponseEntity<ModuleInstallationDto> installModule(
            @PathVariable Long orgId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ModuleInstallRequestDto request) {

        log.info("Installing module {} for organization {}", request.getModuleSlug(), orgId);

        MarketplaceModule module = moduleRepository.findBySlug(request.getModuleSlug())
                .orElseThrow(() -> new IllegalArgumentException("Module not found: " + request.getModuleSlug()));

        // Check if already installed
        if (moduleInstallationRepository.findByOrganizationIdAndModuleId(orgId, module.getId()).isPresent()) {
            throw new IllegalStateException("Module already installed");
        }

        // Create installation record
        ModuleInstallation installation = ModuleInstallation.builder()
                .organizationId(orgId)
                .module(module)
                .installedVersion(module.getVersion())
                .status(ModuleInstallation.InstallationStatus.INSTALLING)
                .installedBy(userId)
                .active(true)
                .trial(request.isStartTrial())
                .settings(request.getConfiguration() != null ? request.getConfiguration() : new HashMap<>())
                .build();

        if (request.isStartTrial() && module.getTrialDays() != null) {
            installation.setTrialEndsAt(Instant.now().plus(module.getTrialDays(), ChronoUnit.DAYS));
        }

        installation = moduleInstallationRepository.save(installation);

        // Load plugin
        try {
            pluginManager.loadPlugin(module.getSlug(), orgId, installation.getSettings());
            installation.setStatus(ModuleInstallation.InstallationStatus.ACTIVE);
            installation.setActivatedAt(Instant.now());
        } catch (Exception e) {
            log.error("Failed to load plugin: {}", e.getMessage());
            installation.setStatus(ModuleInstallation.InstallationStatus.FAILED);
            installation.setLastError(e.getMessage());
        }

        installation = moduleInstallationRepository.save(installation);

        // Update install count
        module.incrementInstalls();
        moduleRepository.save(module);

        return ResponseEntity.ok(toModuleInstallationDto(installation));
    }

    @PostMapping("/modules/{installationId}/activate")
    @Operation(summary = "Activate an installed module")
    @Transactional
    public ResponseEntity<ModuleInstallationDto> activateModule(
            @PathVariable Long orgId,
            @PathVariable Long installationId) {

        ModuleInstallation installation = moduleInstallationRepository.findById(installationId)
                .filter(i -> i.getOrganizationId().equals(orgId))
                .orElseThrow(() -> new IllegalArgumentException("Installation not found"));

        installation.setActive(true);
        installation.setStatus(ModuleInstallation.InstallationStatus.ACTIVE);
        installation.setActivatedAt(Instant.now());

        pluginManager.activatePlugin(installation.getModule().getSlug(), orgId);

        return ResponseEntity.ok(toModuleInstallationDto(moduleInstallationRepository.save(installation)));
    }

    @PostMapping("/modules/{installationId}/deactivate")
    @Operation(summary = "Deactivate an installed module")
    @Transactional
    public ResponseEntity<ModuleInstallationDto> deactivateModule(
            @PathVariable Long orgId,
            @PathVariable Long installationId) {

        ModuleInstallation installation = moduleInstallationRepository.findById(installationId)
                .filter(i -> i.getOrganizationId().equals(orgId))
                .orElseThrow(() -> new IllegalArgumentException("Installation not found"));

        installation.setActive(false);
        installation.setStatus(ModuleInstallation.InstallationStatus.INACTIVE);
        installation.setDeactivatedAt(Instant.now());

        pluginManager.deactivatePlugin(installation.getModule().getSlug(), orgId);

        return ResponseEntity.ok(toModuleInstallationDto(moduleInstallationRepository.save(installation)));
    }

    @PutMapping("/modules/{installationId}/config")
    @Operation(summary = "Update module configuration")
    @Transactional
    public ResponseEntity<ModuleInstallationDto> updateModuleConfig(
            @PathVariable Long orgId,
            @PathVariable Long installationId,
            @RequestBody ModuleConfigUpdateDto configDto) {

        ModuleInstallation installation = moduleInstallationRepository.findById(installationId)
                .filter(i -> i.getOrganizationId().equals(orgId))
                .orElseThrow(() -> new IllegalArgumentException("Installation not found"));

        if (configDto.getSettings() != null) {
            installation.setSettings(configDto.getSettings());
            pluginManager.updateConfiguration(installation.getModule().getSlug(), orgId, configDto.getSettings());
        }

        if (configDto.getActive() != null) {
            installation.setActive(configDto.getActive());
        }

        if (configDto.getAutoUpdate() != null) {
            installation.setAutoUpdate(configDto.getAutoUpdate());
        }

        return ResponseEntity.ok(toModuleInstallationDto(moduleInstallationRepository.save(installation)));
    }

    @DeleteMapping("/modules/{installationId}")
    @Operation(summary = "Uninstall a module")
    @Transactional
    public ResponseEntity<Void> uninstallModule(
            @PathVariable Long orgId,
            @PathVariable Long installationId) {

        ModuleInstallation installation = moduleInstallationRepository.findById(installationId)
                .filter(i -> i.getOrganizationId().equals(orgId))
                .orElseThrow(() -> new IllegalArgumentException("Installation not found"));

        pluginManager.unloadPlugin(installation.getModule().getSlug(), orgId);

        MarketplaceModule module = installation.getModule();
        module.decrementActiveInstalls();
        moduleRepository.save(module);

        moduleInstallationRepository.delete(installation);

        log.info("Uninstalled module {} from organization {}", module.getSlug(), orgId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Course Subscriptions ====================

    @GetMapping("/courses")
    @Operation(summary = "Get subscribed courses")
    public ResponseEntity<List<CourseSubscriptionDto>> getSubscribedCourses(@PathVariable Long orgId) {
        return ResponseEntity.ok(getSubscribedCoursesList(orgId));
    }

    @PostMapping("/courses/subscribe")
    @Operation(summary = "Subscribe to a course")
    @Transactional
    public ResponseEntity<CourseSubscriptionDto> subscribeCourse(
            @PathVariable Long orgId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CoursePurchaseRequestDto request) {

        log.info("Subscribing to course {} for organization {}", request.getCourseSlug(), orgId);

        MarketplaceCourse course = courseRepository.findBySlug(request.getCourseSlug())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + request.getCourseSlug()));

        // Check if already subscribed
        if (courseSubscriptionRepository.findByOrganizationIdAndCourseId(orgId, course.getId()).isPresent()) {
            throw new IllegalStateException("Already subscribed to this course");
        }

        CourseSubscription subscription = CourseSubscription.builder()
                .organizationId(orgId)
                .course(course)
                .status(CourseSubscription.SubscriptionStatus.ACTIVE)
                .purchasedBy(userId)
                .purchasePrice(course.getPrice())
                .purchaseCurrency(course.getPriceCurrency())
                .subscriptionType(request.getSubscriptionType())
                .startsAt(Instant.now())
                .autoRenew(request.isAutoRenew())
                .maxEnrollments(request.getMaxEnrollments())
                .currentEnrollments(0)
                .build();

        // Set expiration based on subscription type
        if (request.getSubscriptionType() == CourseSubscription.SubscriptionType.MONTHLY) {
            subscription.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        } else if (request.getSubscriptionType() == CourseSubscription.SubscriptionType.YEARLY) {
            subscription.setExpiresAt(Instant.now().plus(365, ChronoUnit.DAYS));
        }

        subscription = courseSubscriptionRepository.save(subscription);

        // Update course stats
        course.setEnrollmentCount(course.getEnrollmentCount() + 1);
        courseRepository.save(course);

        return ResponseEntity.ok(toCourseSubscriptionDto(subscription));
    }

    @PostMapping("/courses/{subscriptionId}/import")
    @Operation(summary = "Import course content to organization")
    @Transactional
    public ResponseEntity<CourseSubscriptionDto> importCourseContent(
            @PathVariable Long orgId,
            @PathVariable Long subscriptionId) {

        CourseSubscription subscription = courseSubscriptionRepository.findById(subscriptionId)
                .filter(s -> s.getOrganizationId().equals(orgId))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        // In production: Copy course content to organization's course library
        // This would create a new Course entity in the organization's courses

        subscription.setContentImported(true);
        subscription.setImportedAt(Instant.now());
        // subscription.setCourseCopyId(newCourseId);

        return ResponseEntity.ok(toCourseSubscriptionDto(courseSubscriptionRepository.save(subscription)));
    }

    @DeleteMapping("/courses/{subscriptionId}")
    @Operation(summary = "Cancel course subscription")
    @Transactional
    public ResponseEntity<Void> cancelSubscription(
            @PathVariable Long orgId,
            @PathVariable Long subscriptionId) {

        CourseSubscription subscription = courseSubscriptionRepository.findById(subscriptionId)
                .filter(s -> s.getOrganizationId().equals(orgId))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        subscription.setStatus(CourseSubscription.SubscriptionStatus.CANCELLED);
        courseSubscriptionRepository.save(subscription);

        log.info("Cancelled subscription {} for organization {}", subscriptionId, orgId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Plugin Execution ====================

    @PostMapping("/modules/{moduleSlug}/execute")
    @Operation(summary = "Execute a plugin method")
    public ResponseEntity<Object> executePluginMethod(
            @PathVariable Long orgId,
            @PathVariable String moduleSlug,
            @RequestParam String method,
            @RequestBody(required = false) Map<String, Object> args) {

        // Verify module is installed and active
        moduleInstallationRepository.findByOrganizationIdAndActiveTrue(orgId).stream()
                .filter(i -> i.getModule().getSlug().equals(moduleSlug))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Module not installed or not active"));

        Object[] methodArgs = args != null ? args.values().toArray() : new Object[0];
        Object result = pluginManager.executePlugin(moduleSlug, orgId, method, methodArgs);

        return ResponseEntity.ok(result);
    }

    // ==================== Helper Methods ====================

    private List<ModuleInstallationDto> getInstalledModulesList(Long orgId) {
        return moduleInstallationRepository.findByOrganizationId(orgId).stream()
                .map(this::toModuleInstallationDto)
                .collect(Collectors.toList());
    }

    private List<CourseSubscriptionDto> getSubscribedCoursesList(Long orgId) {
        return courseSubscriptionRepository.findByOrganizationId(orgId).stream()
                .map(this::toCourseSubscriptionDto)
                .collect(Collectors.toList());
    }

    private ModuleInstallationDto toModuleInstallationDto(ModuleInstallation i) {
        return ModuleInstallationDto.builder()
                .id(i.getId())
                .moduleId(i.getModule().getId())
                .moduleName(i.getModule().getName())
                .moduleSlug(i.getModule().getSlug())
                .installedVersion(i.getInstalledVersion())
                .status(i.getStatus())
                .settings(i.getSettings())
                .active(i.isActive())
                .trial(i.isTrial())
                .trialEndsAt(i.getTrialEndsAt())
                .updateAvailable(i.isUpdateAvailable())
                .availableVersion(i.getAvailableVersion())
                .installedAt(i.getInstalledAt())
                .lastUsedAt(i.getLastUsedAt())
                .usageCount(i.getUsageCount())
                .build();
    }

    private CourseSubscriptionDto toCourseSubscriptionDto(CourseSubscription s) {
        return CourseSubscriptionDto.builder()
                .id(s.getId())
                .courseId(s.getCourse().getId())
                .courseName(s.getCourse().getName())
                .courseSlug(s.getCourse().getSlug())
                .status(s.getStatus())
                .subscriptionType(s.getSubscriptionType())
                .purchasePrice(s.getPurchasePrice())
                .startsAt(s.getStartsAt())
                .expiresAt(s.getExpiresAt())
                .autoRenew(s.isAutoRenew())
                .maxEnrollments(s.getMaxEnrollments())
                .currentEnrollments(s.getCurrentEnrollments())
                .remainingSeats(s.getRemainingSeats())
                .courseCopyId(s.getCourseCopyId())
                .contentImported(s.isContentImported())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
