package com.freelms.bot.whatsapp.service;

import com.freelms.bot.whatsapp.model.ApiResponse;
import com.freelms.bot.whatsapp.model.Course;
import com.freelms.bot.whatsapp.model.EnrollmentResponse;
import com.freelms.bot.whatsapp.model.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with the FREE LMS API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LmsApiService {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(1);

    private final WebClient webClient;

    /**
     * Fetches a paginated list of courses from the API.
     *
     * @param page page number (0-based)
     * @param size number of courses per page
     * @return list of courses, or empty list if API is unavailable
     */
    public List<Course> getCourses(int page, int size) {
        log.debug("Fetching courses: page={}, size={}", page, size);
        try {
            ApiResponse<PagedResponse<Map<String, Object>>> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/courses")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        log.error("API error fetching courses: status={}", clientResponse.statusCode());
                        return Mono.empty();
                    })
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<Map<String, Object>>>>() {})
                    .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, RETRY_DELAY)
                            .filter(this::isRetryableException)
                            .doBeforeRetry(signal -> log.warn("Retrying getCourses, attempt {}", signal.totalRetries() + 1)))
                    .onErrorResume(e -> {
                        log.error("Error fetching courses: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response != null && response.isSuccess() && response.getData() != null) {
                List<Course> courses = response.getData().getContent().stream()
                        .map(this::mapToCourse)
                        .toList();
                log.info("Successfully fetched {} courses", courses.size());
                return courses;
            }

            log.warn("No courses returned from API");
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching courses from API", e);
            return Collections.emptyList();
        }
    }

    /**
     * Fetches the user's enrolled courses using JWT authentication.
     *
     * @param accessToken JWT access token for authentication
     * @return list of enrolled courses, or empty list if unavailable
     */
    public List<Course> getUserCourses(String accessToken) {
        log.debug("Fetching user courses with authentication");

        if (accessToken == null || accessToken.isBlank()) {
            log.warn("No access token provided for getUserCourses");
            return Collections.emptyList();
        }

        try {
            ApiResponse<PagedResponse<EnrollmentResponse>> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/enrollments/my")
                            .queryParam("page", 0)
                            .queryParam("size", 20)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        log.error("API error fetching user courses: status={}", clientResponse.statusCode());
                        return Mono.empty();
                    })
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<PagedResponse<EnrollmentResponse>>>() {})
                    .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, RETRY_DELAY)
                            .filter(this::isRetryableException)
                            .doBeforeRetry(signal -> log.warn("Retrying getUserCourses, attempt {}", signal.totalRetries() + 1)))
                    .onErrorResume(e -> {
                        log.error("Error fetching user courses: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response != null && response.isSuccess() && response.getData() != null) {
                List<Course> courses = response.getData().getContent().stream()
                        .map(this::mapEnrollmentToCourse)
                        .toList();
                log.info("Successfully fetched {} user courses", courses.size());
                return courses;
            }

            log.warn("No user courses returned from API");
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching user courses from API", e);
            return Collections.emptyList();
        }
    }

    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            return statusCode == 503 || statusCode == 502 || statusCode == 504;
        }
        return throwable instanceof java.net.ConnectException
                || throwable instanceof java.net.SocketTimeoutException;
    }

    private Course mapToCourse(Map<String, Object> data) {
        Course course = new Course();
        course.setId(getLongValue(data, "id", 0L));
        course.setTitle(getStringValue(data, "title", "Untitled"));
        course.setDescription(getStringValue(data, "description", ""));
        course.setLevel(getStringValue(data, "level", "Beginner"));
        course.setThumbnailUrl(getStringValue(data, "thumbnailUrl", null));

        Object priceObj = data.get("price");
        if (priceObj instanceof Number num) {
            course.setPrice(new BigDecimal(num.toString()));
        }

        course.setFree(Boolean.TRUE.equals(data.get("free")));

        Object lessonCount = data.get("lessonCount");
        if (lessonCount instanceof Number num) {
            course.setTotalLessons(num.intValue());
        }

        Object durationMinutes = data.get("durationMinutes");
        if (durationMinutes instanceof Number num) {
            course.setTotalDuration(num.intValue());
        }

        return course;
    }

    private Course mapEnrollmentToCourse(EnrollmentResponse enrollment) {
        Course course = new Course();
        course.setId(enrollment.getCourseId());
        course.setTitle(enrollment.getCourseTitle());
        course.setThumbnailUrl(enrollment.getCourseThumbnail());
        course.setTotalLessons(enrollment.getTotalLessons());
        return course;
    }

    private Long getLongValue(Map<String, Object> data, String key, Long defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number num) {
            return num.longValue();
        }
        return defaultValue;
    }

    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        if (value instanceof String str) {
            return str;
        }
        return defaultValue;
    }
}
