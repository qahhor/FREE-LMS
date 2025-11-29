package com.freelms.bot.telegram.service;

import com.freelms.bot.telegram.config.BotConfig;
import com.freelms.bot.telegram.model.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LmsApiService {

    private final BotConfig botConfig;
    private final WebClient.Builder webClientBuilder;

    public List<Course> getCourses(int page, int limit) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(botConfig.getApiUrl()).build();

            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/courses")
                            .queryParam("page", page)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .onErrorResume(e -> {
                        log.error("Error fetching courses: {}", e.getMessage());
                        return Mono.just(Collections.emptyMap());
                    })
                    .block();

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> coursesData = (List<Map<String, Object>>) response.get("data");
                return coursesData.stream()
                        .map(this::mapToCourse)
                        .toList();
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching courses from API", e);
            return Collections.emptyList();
        }
    }

    public List<Course> getUserCourses(String accessToken) {
        // TODO: Implement with actual API authentication
        // For now, return demo data
        Course course1 = new Course();
        course1.setId(1L);
        course1.setTitle("Introduction to Programming");
        course1.setLevel("Beginner");
        course1.setFree(true);

        Course course2 = new Course();
        course2.setId(2L);
        course2.setTitle("Web Development Basics");
        course2.setLevel("Beginner");
        course2.setFree(false);

        return List.of(course1, course2);
    }

    private Course mapToCourse(Map<String, Object> data) {
        Course course = new Course();
        course.setId(((Number) data.getOrDefault("id", 0L)).longValue());
        course.setTitle((String) data.getOrDefault("title", "Untitled"));
        course.setDescription((String) data.getOrDefault("description", ""));
        course.setLevel((String) data.getOrDefault("level", "Beginner"));

        Object priceObj = data.get("price");
        if (priceObj instanceof Number) {
            course.setPrice(new java.math.BigDecimal(priceObj.toString()));
        }

        course.setFree(Boolean.TRUE.equals(data.get("isFree")));
        course.setThumbnailUrl((String) data.get("thumbnailUrl"));

        return course;
    }
}
