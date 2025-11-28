package com.freelms.search.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Fallback for Course Service client.
 */
@Component
public class CourseServiceClientFallback implements CourseServiceClient {

    private static final Logger log = LoggerFactory.getLogger(CourseServiceClientFallback.class);

    @Override
    public List<Map<String, Object>> getCoursesForIndexing(int page, int size) {
        log.warn("Course service unavailable, returning empty list");
        return Collections.emptyList();
    }

    @Override
    public Long getCoursesCount() {
        log.warn("Course service unavailable, returning 0");
        return 0L;
    }

    @Override
    public List<Map<String, Object>> getLessonsForIndexing(int page, int size) {
        log.warn("Course service unavailable, returning empty list");
        return Collections.emptyList();
    }
}
