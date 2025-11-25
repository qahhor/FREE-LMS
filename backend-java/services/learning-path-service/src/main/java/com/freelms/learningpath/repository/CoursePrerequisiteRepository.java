package com.freelms.learningpath.repository;

import com.freelms.learningpath.entity.CoursePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, Long> {

    List<CoursePrerequisite> findByCourseId(Long courseId);

    List<CoursePrerequisite> findByPrerequisiteCourseId(Long prerequisiteCourseId);

    boolean existsByCourseIdAndPrerequisiteCourseId(Long courseId, Long prerequisiteCourseId);

    void deleteByCourseIdAndPrerequisiteCourseId(Long courseId, Long prerequisiteCourseId);
}
