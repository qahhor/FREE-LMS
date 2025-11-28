package com.freelms.media.repository;

import com.freelms.media.entity.MediaVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for MediaVariant entities.
 */
@Repository
public interface MediaVariantRepository extends JpaRepository<MediaVariant, UUID> {

    List<MediaVariant> findByMediaFileId(UUID mediaFileId);

    List<MediaVariant> findByMediaFileIdAndQuality(UUID mediaFileId, String quality);

    void deleteByMediaFileId(UUID mediaFileId);
}
