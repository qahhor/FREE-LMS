package com.freelms.lti.repository;

import com.freelms.lti.entity.LtiPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LtiPlatformRepository extends JpaRepository<LtiPlatform, UUID> {

    Optional<LtiPlatform> findByIssuer(String issuer);

    Optional<LtiPlatform> findByIssuerAndClientId(String issuer, String clientId);

    List<LtiPlatform> findByOrganizationId(Long organizationId);

    List<LtiPlatform> findByStatus(LtiPlatform.PlatformStatus status);
}
