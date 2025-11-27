package com.freelms.media.repository;

import com.freelms.media.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for MediaFile entities.
 */
@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

    /**
     * Find all media files by course ID.
     */
    List<MediaFile> findByCourseId(Long courseId);

    /**
     * Find all media files by lesson ID.
     */
    List<MediaFile> findByLessonId(Long lessonId);

    /**
     * Find all media files by uploaded user.
     */
    List<MediaFile> findByUploadedBy(Long userId);

    /**
     * Find all media files by organization.
     */
    List<MediaFile> findByOrganizationId(Long organizationId);

    /**
     * Find all media files with a specific processing status.
     */
    List<MediaFile> findByProcessingStatus(MediaFile.ProcessingStatus status);

    /**
     * Count media files by processing status.
     */
    long countByProcessingStatus(MediaFile.ProcessingStatus status);
}
