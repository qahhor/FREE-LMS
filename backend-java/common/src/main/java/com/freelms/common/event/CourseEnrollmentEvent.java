package com.freelms.common.event;

import lombok.Getter;

@Getter
public class CourseEnrollmentEvent extends DomainEvent {

    private final Long userId;
    private final Long courseId;
    private final String courseName;

    public CourseEnrollmentEvent(Long userId, Long courseId, String courseName) {
        super();
        this.userId = userId;
        this.courseId = courseId;
        this.courseName = courseName;
    }
}
