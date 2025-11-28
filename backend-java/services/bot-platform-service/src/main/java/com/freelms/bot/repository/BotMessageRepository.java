package com.freelms.bot.repository;

import com.freelms.bot.entity.BotMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BotMessageRepository extends JpaRepository<BotMessage, UUID> {

    Page<BotMessage> findByBotUserId(UUID botUserId, Pageable pageable);

    List<BotMessage> findByBotUserIdOrderByTimestampDesc(UUID botUserId);

    @Query("SELECT m FROM BotMessage m WHERE m.botUser.id = :userId " +
           "AND m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<BotMessage> findRecentMessages(
            @Param("userId") UUID userId,
            @Param("since") LocalDateTime since);

    @Query("SELECT m FROM BotMessage m WHERE m.deliveryStatus = 'PENDING' " +
           "AND m.direction = 'OUTBOUND' ORDER BY m.timestamp")
    List<BotMessage> findPendingOutbound();

    @Query("SELECT m FROM BotMessage m WHERE m.deliveryStatus = 'FAILED' " +
           "AND m.timestamp >= :since")
    List<BotMessage> findFailedMessages(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(m) FROM BotMessage m WHERE m.botConfig.id = :configId " +
           "AND m.timestamp >= :since")
    Long countMessages(
            @Param("configId") UUID configId,
            @Param("since") LocalDateTime since);

    @Query("SELECT m.notificationType, COUNT(m) FROM BotMessage m " +
           "WHERE m.direction = 'OUTBOUND' AND m.timestamp >= :since " +
           "GROUP BY m.notificationType")
    List<Object[]> countByNotificationType(@Param("since") LocalDateTime since);
}
