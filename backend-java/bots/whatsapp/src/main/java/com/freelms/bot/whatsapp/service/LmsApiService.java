package com.freelms.bot.whatsapp.service;

import com.freelms.bot.whatsapp.model.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LmsApiService {

    private final WebClient webClient;

    public List<Course> getCourses(int page, int size) {
        try {
            // TODO: Implement real API call when backend is ready
            // return webClient.get()
            //         .uri("/courses?page={page}&size={size}", page, size)
            //         .retrieve()
            //         .bodyToFlux(Course.class)
            //         .collectList()
            //         .block();

            // Demo data for testing
            return getDemoCourses();
        } catch (Exception e) {
            log.error("Error fetching courses", e);
            return getDemoCourses();
        }
    }

    public List<Course> getUserCourses(String userId) {
        try {
            // TODO: Implement real API call when backend is ready
            // return webClient.get()
            //         .uri("/users/{userId}/courses", userId)
            //         .retrieve()
            //         .bodyToFlux(Course.class)
            //         .collectList()
            //         .block();

            return getDemoUserCourses();
        } catch (Exception e) {
            log.error("Error fetching user courses", e);
            return getDemoUserCourses();
        }
    }

    private List<Course> getDemoCourses() {
        List<Course> courses = new ArrayList<>();

        Course course1 = new Course();
        course1.setId(1L);
        course1.setTitle("Java Spring Boot Masterclass");
        course1.setLevel("Intermediate");
        course1.setPrice(new BigDecimal("49.99"));
        course1.setFree(false);
        courses.add(course1);

        Course course2 = new Course();
        course2.setId(2L);
        course2.setTitle("React.js Complete Guide");
        course2.setLevel("Beginner");
        course2.setPrice(BigDecimal.ZERO);
        course2.setFree(true);
        courses.add(course2);

        Course course3 = new Course();
        course3.setId(3L);
        course3.setTitle("DevOps with Docker & Kubernetes");
        course3.setLevel("Advanced");
        course3.setPrice(new BigDecimal("79.99"));
        course3.setFree(false);
        courses.add(course3);

        Course course4 = new Course();
        course4.setId(4L);
        course4.setTitle("Python for Data Science");
        course4.setLevel("Beginner");
        course4.setPrice(BigDecimal.ZERO);
        course4.setFree(true);
        courses.add(course4);

        Course course5 = new Course();
        course5.setId(5L);
        course5.setTitle("PostgreSQL Advanced Queries");
        course5.setLevel("Advanced");
        course5.setPrice(new BigDecimal("29.99"));
        course5.setFree(false);
        courses.add(course5);

        return courses;
    }

    private List<Course> getDemoUserCourses() {
        List<Course> courses = new ArrayList<>();

        Course course1 = new Course();
        course1.setId(1L);
        course1.setTitle("Java Spring Boot Masterclass");
        course1.setLevel("Intermediate");
        courses.add(course1);

        Course course2 = new Course();
        course2.setId(2L);
        course2.setTitle("React.js Complete Guide");
        course2.setLevel("Beginner");
        courses.add(course2);

        return courses;
    }
}
