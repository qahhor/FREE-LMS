package com.freelms.course.repository;

import com.freelms.course.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT t FROM Tag t JOIN t.courses c GROUP BY t ORDER BY COUNT(c) DESC")
    List<Tag> findPopularTags();
}
