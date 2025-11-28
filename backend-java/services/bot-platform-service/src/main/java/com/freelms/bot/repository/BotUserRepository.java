package com.freelms.bot.repository;

import com.freelms.bot.entity.BotConfiguration;
import com.freelms.bot.entity.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BotUserRepository extends JpaRepository<BotUser, UUID> {

    Optional<BotUser> findByPlatformAndPlatformUserId(
            BotConfiguration.BotPlatform platform, String platformUserId);

    List<BotUser> findByLmsUserId(Long lmsUserId);

    Optional<BotUser> findByLmsUserIdAndPlatform(
            Long lmsUserId, BotConfiguration.BotPlatform platform);

    List<BotUser> findByPlatform(BotConfiguration.BotPlatform platform);

    List<BotUser> findByStatus(BotUser.UserStatus status);

    @Query("SELECT u FROM BotUser u WHERE u.lmsUserId = :userId " +
           "AND u.notificationsEnabled = true AND u.status = 'ACTIVE'")
    List<BotUser> findActiveSubscribers(@Param("userId") Long userId);

    @Query("SELECT u FROM BotUser u WHERE u.lmsUserId = :userId " +
           "AND u.platform = :platform AND u.notificationsEnabled = true")
    Optional<BotUser> findNotifiableUser(
            @Param("userId") Long userId,
            @Param("platform") BotConfiguration.BotPlatform platform);

    @Query("SELECT u FROM BotUser u WHERE u.verificationCode = :code " +
           "AND u.verificationExpires > CURRENT_TIMESTAMP")
    Optional<BotUser> findByVerificationCode(@Param("code") String code);
}
