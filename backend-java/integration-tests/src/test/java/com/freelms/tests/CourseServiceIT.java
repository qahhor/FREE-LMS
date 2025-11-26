package com.freelms.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Smartup LMS - Course Service Integration Tests
 *
 * Tests course creation, management, and content operations.
 */
@TestMethodOrder(OrderAnnotation.class)
@Tag("integration")
@Tag("course")
public class CourseServiceIT extends BaseIntegrationTest {

    private static String courseId;
    private static String moduleId;
    private static String lessonId;

    @Test
    @Order(1)
    @DisplayName("Create new course")
    void testCreateCourse() {
        Map<String, Object> course = new HashMap<>();
        course.put("title", "Integration Test Course");
        course.put("description", "A course created during integration testing");
        course.put("category", "TECHNOLOGY");
        course.put("level", "BEGINNER");
        course.put("language", "en");
        course.put("duration", 120);
        course.put("price", 99.99);
        course.put("tags", Arrays.asList("integration", "test", "java"));

        Response response = adminRequest()
                .body(course)
                .when()
                .post("/api/courses")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("Integration Test Course"))
                .body("status", equalTo("DRAFT"))
                .body("instructor.id", notNullValue())
                .extract()
                .response();

        courseId = response.path("id");
        log.info("Created course with ID: {}", courseId);
    }

    @Test
    @Order(2)
    @DisplayName("Get course by ID")
    void testGetCourse() {
        authenticatedRequest()
                .when()
                .get("/api/courses/" + courseId)
                .then()
                .statusCode(200)
                .body("id", equalTo(courseId))
                .body("title", equalTo("Integration Test Course"));
    }

    @Test
    @Order(3)
    @DisplayName("Update course")
    void testUpdateCourse() {
        Map<String, Object> update = new HashMap<>();
        update.put("title", "Updated Integration Test Course");
        update.put("description", "Updated description");

        adminRequest()
                .body(update)
                .when()
                .put("/api/courses/" + courseId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Integration Test Course"))
                .body("description", equalTo("Updated description"));
    }

    @Test
    @Order(4)
    @DisplayName("Add module to course")
    void testAddModule() {
        Map<String, Object> module = new HashMap<>();
        module.put("title", "Introduction Module");
        module.put("description", "First module of the course");
        module.put("order", 1);

        Response response = adminRequest()
                .body(module)
                .when()
                .post("/api/courses/" + courseId + "/modules")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("Introduction Module"))
                .extract()
                .response();

        moduleId = response.path("id");
        log.info("Created module with ID: {}", moduleId);
    }

    @Test
    @Order(5)
    @DisplayName("Add lesson to module")
    void testAddLesson() {
        Map<String, Object> lesson = new HashMap<>();
        lesson.put("title", "Getting Started");
        lesson.put("type", "VIDEO");
        lesson.put("content", "https://video.freelms.com/intro.mp4");
        lesson.put("duration", 15);
        lesson.put("order", 1);

        Response response = adminRequest()
                .body(lesson)
                .when()
                .post("/api/courses/" + courseId + "/modules/" + moduleId + "/lessons")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("Getting Started"))
                .body("type", equalTo("VIDEO"))
                .extract()
                .response();

        lessonId = response.path("id");
        log.info("Created lesson with ID: {}", lessonId);
    }

    @Test
    @Order(6)
    @DisplayName("Get course structure")
    void testGetCourseStructure() {
        authenticatedRequest()
                .when()
                .get("/api/courses/" + courseId + "/structure")
                .then()
                .statusCode(200)
                .body("modules", hasSize(1))
                .body("modules[0].lessons", hasSize(1))
                .body("modules[0].title", equalTo("Introduction Module"))
                .body("modules[0].lessons[0].title", equalTo("Getting Started"));
    }

    @Test
    @Order(7)
    @DisplayName("Publish course")
    void testPublishCourse() {
        adminRequest()
                .when()
                .post("/api/courses/" + courseId + "/publish")
                .then()
                .statusCode(200)
                .body("status", equalTo("PUBLISHED"));
    }

    @Test
    @Order(8)
    @DisplayName("Search courses")
    void testSearchCourses() {
        authenticatedRequest()
                .queryParam("q", "Integration")
                .queryParam("category", "TECHNOLOGY")
                .queryParam("level", "BEGINNER")
                .when()
                .get("/api/courses/search")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .body("content[0].title", containsString("Integration"));
    }

    @Test
    @Order(9)
    @DisplayName("Get course catalog with pagination")
    void testGetCatalog() {
        authenticatedRequest()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sort", "createdAt,desc")
                .when()
                .get("/api/courses/catalog")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(1))
                .body("pageable.pageSize", equalTo(10));
    }

    @Test
    @Order(10)
    @DisplayName("Get course statistics")
    void testGetCourseStats() {
        adminRequest()
                .when()
                .get("/api/courses/" + courseId + "/statistics")
                .then()
                .statusCode(200)
                .body("enrollmentCount", greaterThanOrEqualTo(0))
                .body("completionRate", greaterThanOrEqualTo(0.0f))
                .body("averageRating", greaterThanOrEqualTo(0.0f));
    }
}
