package com.freelms.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import java.util.concurrent.TimeUnit;

/**
 * FREE LMS - End-to-End Learner Journey Tests
 *
 * Tests complete user workflows from registration to course completion.
 * Validates the entire learning experience across multiple services.
 */
@TestMethodOrder(OrderAnnotation.class)
@Tag("e2e")
@Tag("integration")
public class E2ELearnerJourneyIT extends BaseIntegrationTest {

    private static String learnerEmail;
    private static String learnerToken;
    private static String learnerId;
    private static String courseId;
    private static String enrollmentId;

    @BeforeAll
    static void setupJourney() {
        learnerEmail = "learner." + UUID.randomUUID().toString().substring(0, 8) + "@freelms.com";
    }

    @Test
    @Order(1)
    @DisplayName("E2E: New learner registers")
    void step1_LearnerRegistration() {
        Map<String, Object> registration = new HashMap<>();
        registration.put("email", learnerEmail);
        registration.put("password", "LearnerPass123!");
        registration.put("firstName", "John");
        registration.put("lastName", "Learner");
        registration.put("organizationId", "e2e-test-org");

        Response response = given()
                .spec(requestSpec)
                .body(registration)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("role", equalTo("LEARNER"))
                .extract()
                .response();

        learnerId = response.path("id");
        log.info("E2E Step 1: Learner registered - {}", learnerEmail);
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Learner logs in")
    void step2_LearnerLogin() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", learnerEmail);
        credentials.put("password", "LearnerPass123!");

        Response response = given()
                .spec(requestSpec)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .extract()
                .response();

        learnerToken = response.path("accessToken");
        log.info("E2E Step 2: Learner logged in successfully");
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Learner browses course catalog")
    void step3_BrowseCatalog() {
        Response response = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + learnerToken)
                .queryParam("status", "PUBLISHED")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/courses/catalog")
                .then()
                .statusCode(200)
                .body("content", not(empty()))
                .extract()
                .response();

        // Get first available course
        List<Map<String, Object>> courses = response.path("content");
        if (!courses.isEmpty()) {
            courseId = (String) courses.get(0).get("id");
            log.info("E2E Step 3: Found course to enroll - {}", courseId);
        }
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Learner views course details")
    void step4_ViewCourseDetails() {
        Assumptions.assumeTrue(courseId != null, "No course available for testing");

        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + learnerToken)
                .when()
                .get("/api/courses/" + courseId)
                .then()
                .statusCode(200)
                .body("id", equalTo(courseId))
                .body("title", notNullValue())
                .body("modules", notNullValue());

        log.info("E2E Step 4: Viewed course details");
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Learner enrolls in course")
    void step5_EnrollInCourse() {
        Assumptions.assumeTrue(courseId != null, "No course available for testing");

        Response response = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + learnerToken)
                .when()
                .post("/api/enrollments/courses/" + courseId)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("courseId", equalTo(courseId))
                .body("userId", equalTo(learnerId))
                .body("status", equalTo("ACTIVE"))
                .body("progress", equalTo(0))
                .extract()
                .response();

        enrollmentId = response.path("id");
        log.info("E2E Step 5: Enrolled in course - enrollment ID: {}", enrollmentId);
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Learner views enrolled courses")
    void step6_ViewEnrolledCourses() {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + learnerToken)
                .when()
                .get("/api/enrollments/my-courses")
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThanOrEqualTo(1)))
                .body("content.find { it.id == '" + enrollmentId + "' }", notNullValue());

        log.info("E2E Step 6: Verified enrolled courses");
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Learner starts learning (completes first lesson)")
    void step7_CompleteFirstLesson() {
        Assumptions.assumeTrue(enrollmentId != null, "No enrollment for testing");

        // Get course structure to find first lesson
        Response structureResponse = given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + learnerToken)
                .when()
                .get("/api/courses/" + courseId + "/structure")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Map<String, Object>> modules = structureResponse.path("modules");
        Assumptions.assumeTrue(modules != null && !modules.isEmpty(), "No modules in course");

        List<Map<String, Object>> lessons = (List<Map<String, Object>>) modules.get(0).get("lessons");
        Assumptions.assumeTrue(lessons != null && !lessons.isEmpty(), "No lessons in module");

        String lessonId = (String) lessons.get(0).get("id");

        // Mark lesson as completed
        Map<String, Object> progress = new HashMap<>();
        progress.put("lessonId", lessonId);
        progress.put("completed", true);
        progress.put("timeSpent", 900); // 15 minutes in seconds

        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + learnerToken)
                .body(progress)
                .when()
                .post("/api/enrollments/" + enrollmentId + "/progress")
                .then()
                .statusCode(200)
                .body("progress", greaterThan(0));

        log.info("E2E Step 7: Completed first lesson");
    }

    @Test
    @Order(8)
    @DisplayName("E2E: Learner checks their progress")
    void step8_CheckProgress() {
        Assumptions.assumeTrue(enrollmentId != null, "No enrollment for testing");

        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + learnerToken)
                .when()
                .get("/api/enrollments/" + enrollmentId + "/progress")
                .then()
                .statusCode(200)
                .body("overallProgress", greaterThan(0))
                .body("completedLessons", greaterThanOrEqualTo(1))
                .body("lastAccessedAt", notNullValue());

        log.info("E2E Step 8: Checked learning progress");
    }

    @Test
    @Order(9)
    @DisplayName("E2E: Learner earns achievement")
    void step9_EarnAchievement() {
        // Wait for gamification service to process
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    Response response = given()
                            .spec(requestSpec)
                            .header("Authorization", "Bearer " + learnerToken)
                            .when()
                            .get("/api/gamification/achievements")
                            .then()
                            .extract()
                            .response();

                    List<Map<String, Object>> achievements = response.path("content");
                    return achievements != null && !achievements.isEmpty();
                });

        log.info("E2E Step 9: Achievement earned");
    }

    @Test
    @Order(10)
    @DisplayName("E2E: Learner submits course feedback")
    void step10_SubmitFeedback() {
        Assumptions.assumeTrue(courseId != null, "No course for feedback");

        Map<String, Object> feedback = new HashMap<>();
        feedback.put("courseId", courseId);
        feedback.put("rating", 5);
        feedback.put("comment", "Excellent course! Very comprehensive and well-structured.");
        feedback.put("wouldRecommend", true);

        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + learnerToken)
                .body(feedback)
                .when()
                .post("/api/feedback/courses")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("rating", equalTo(5));

        log.info("E2E Step 10: Course feedback submitted");
    }

    @Test
    @Order(11)
    @DisplayName("E2E: Learner views their dashboard")
    void step11_ViewDashboard() {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + learnerToken)
                .when()
                .get("/api/analytics/dashboard")
                .then()
                .statusCode(200)
                .body("enrolledCourses", greaterThanOrEqualTo(1))
                .body("totalLearningTime", greaterThanOrEqualTo(0))
                .body("achievements", notNullValue())
                .body("recentActivity", notNullValue());

        log.info("E2E Step 11: Dashboard loaded successfully");
    }

    @Test
    @Order(12)
    @DisplayName("E2E: Learner receives notification")
    void step12_CheckNotifications() {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + learnerToken)
                .when()
                .get("/api/notifications")
                .then()
                .statusCode(200)
                .body("content", notNullValue());

        log.info("E2E Step 12: Notifications retrieved");
    }

    @Test
    @Order(13)
    @DisplayName("E2E: Learner journey completed - cleanup")
    void step13_Cleanup() {
        log.info("E2E Test Journey completed successfully for learner: {}", learnerEmail);
        log.info("  - User ID: {}", learnerId);
        log.info("  - Course ID: {}", courseId);
        log.info("  - Enrollment ID: {}", enrollmentId);
    }
}
