package com.freelms.search.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Feign client for Course Service - used for reindexing.
 */
@FeignClient(name = "course-service", fallback = CourseServiceClientFallback.class)
public interface CourseServiceClient {

    @GetMapping("/api/v1/courses/search-index")
    List<Map<String, Object>> getCoursesForIndexing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size);

    @GetMapping("/api/v1/courses/search-index/count")
    Long getCoursesCount();

    @GetMapping("/api/v1/lessons/search-index")
    List<Map<String, Object>> getLessonsForIndexing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size);
}
