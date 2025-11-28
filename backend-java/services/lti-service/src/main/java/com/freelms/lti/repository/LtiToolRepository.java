package com.freelms.lti.repository;

import com.freelms.lti.entity.LtiTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LtiToolRepository extends JpaRepository<LtiTool, UUID> {

    Optional<LtiTool> findByClientId(String clientId);

    List<LtiTool> findByOrganizationId(Long organizationId);

    List<LtiTool> findByStatus(LtiTool.ToolStatus status);

    List<LtiTool> findByPlacement(LtiTool.ToolPlacement placement);

    List<LtiTool> findByOrganizationIdAndStatus(Long organizationId, LtiTool.ToolStatus status);
}
