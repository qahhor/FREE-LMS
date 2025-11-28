package com.freelms.authoring.repository;

import com.freelms.authoring.entity.InteractiveContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for InteractiveContent entities.
 */
@Repository
public interface InteractiveContentRepository extends JpaRepository<InteractiveContent, UUID> {

    Page<InteractiveContent> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<InteractiveContent> findByAuthorId(Long authorId, Pageable pageable);

    Page<InteractiveContent> findByCourseId(Long courseId, Pageable pageable);

    List<InteractiveContent> findByLessonId(Long lessonId);

    Page<InteractiveContent> findByContentType(InteractiveContent.ContentType contentType, Pageable pageable);

    Page<InteractiveContent> findByStatus(InteractiveContent.ContentStatus status, Pageable pageable);

    List<InteractiveContent> findByParentContentIdOrderByVersionDesc(UUID parentContentId);

    Page<InteractiveContent> findByOrganizationIdAndStatus(Long organizationId,
            InteractiveContent.ContentStatus status, Pageable pageable);
}
