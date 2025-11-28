package com.freelms.bot.repository;

import com.freelms.bot.entity.BotConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BotConfigurationRepository extends JpaRepository<BotConfiguration, UUID> {

    List<BotConfiguration> findByOrganizationId(Long organizationId);

    List<BotConfiguration> findByPlatform(BotConfiguration.BotPlatform platform);

    List<BotConfiguration> findByStatus(BotConfiguration.BotStatus status);

    Optional<BotConfiguration> findByOrganizationIdAndPlatform(
            Long organizationId, BotConfiguration.BotPlatform platform);

    List<BotConfiguration> findByOrganizationIdAndStatus(
            Long organizationId, BotConfiguration.BotStatus status);
}
