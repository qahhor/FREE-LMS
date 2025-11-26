package com.freelms.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * FREE LMS - Auth Service Integration Tests
 *
 * Tests authentication, registration, and user management flows.
 */
@TestMethodOrder(OrderAnnotation.class)
@Tag("integration")
@Tag("auth")
public class AuthServiceIT extends BaseIntegrationTest {

    private static String testUserEmail;
    private static String testUserId;
    private static String testUserToken;

    @BeforeAll
    static void setup() {
        testUserEmail = "integration.test." + UUID.randomUUID().toString().substring(0, 8) + "@freelms.com";
    }

    @Test
    @Order(1)
    @DisplayName("Register new user")
    void testUserRegistration() {
        Map<String, Object> registration = new HashMap<>();
        registration.put("email", testUserEmail);
        registration.put("password", "SecurePassword123!");
        registration.put("firstName", "Integration");
        registration.put("lastName", "Test");
        registration.put("organizationId", "test-org-001");

        Response response = given()
                .spec(requestSpec)
                .body(registration)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("email", equalTo(testUserEmail))
                .body("firstName", equalTo("Integration"))
                .body("lastName", equalTo("Test"))
                .body("role", equalTo("LEARNER"))
                .extract()
                .response();

        testUserId = response.path("id");
        log.info("Created test user: {} with ID: {}", testUserEmail, testUserId);
    }

    @Test
    @Order(2)
    @DisplayName("Login with valid credentials")
    void testLogin() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", testUserEmail);
        credentials.put("password", "SecurePassword123!");

        Response response = given()
                .spec(requestSpec)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("expiresIn", greaterThan(0))
                .body("tokenType", equalTo("Bearer"))
                .extract()
                .response();

        testUserToken = response.path("accessToken");
        log.info("Login successful for: {}", testUserEmail);
    }

    @Test
    @Order(3)
    @DisplayName("Login with invalid credentials returns 401")
    void testLoginInvalidCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", testUserEmail);
        credentials.put("password", "WrongPassword123!");

        given()
                .spec(requestSpec)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("error", equalTo("INVALID_CREDENTIALS"));
    }

    @Test
    @Order(4)
    @DisplayName("Get current user profile")
    void testGetProfile() {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + testUserToken)
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(200)
                .body("id", equalTo(testUserId))
                .body("email", equalTo(testUserEmail))
                .body("firstName", equalTo("Integration"))
                .body("lastName", equalTo("Test"));
    }

    @Test
    @Order(5)
    @DisplayName("Update user profile")
    void testUpdateProfile() {
        Map<String, String> update = new HashMap<>();
        update.put("firstName", "Updated");
        update.put("lastName", "Name");

        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + testUserToken)
                .body(update)
                .when()
                .put("/api/auth/me")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Updated"))
                .body("lastName", equalTo("Name"));
    }

    @Test
    @Order(6)
    @DisplayName("Change password")
    void testChangePassword() {
        Map<String, String> passwordChange = new HashMap<>();
        passwordChange.put("currentPassword", "SecurePassword123!");
        passwordChange.put("newPassword", "NewSecurePassword456!");

        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + testUserToken)
                .body(passwordChange)
                .when()
                .post("/api/auth/change-password")
                .then()
                .statusCode(200);

        // Verify new password works
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", testUserEmail);
        credentials.put("password", "NewSecurePassword456!");

        given()
                .spec(requestSpec)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(7)
    @DisplayName("Refresh token")
    void testRefreshToken() {
        // First login to get refresh token
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", testUserEmail);
        credentials.put("password", "NewSecurePassword456!");

        String refreshToken = given()
                .spec(requestSpec)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("refreshToken");

        // Use refresh token
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", refreshToken);

        given()
                .spec(requestSpec)
                .body(refreshRequest)
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @Order(8)
    @DisplayName("Logout")
    void testLogout() {
        given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + testUserToken)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(9)
    @DisplayName("Access protected endpoint without token returns 401")
    void testUnauthorizedAccess() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(10)
    @DisplayName("Rate limiting is enforced")
    void testRateLimiting() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "ratelimit@test.com");
        credentials.put("password", "wrong");

        // Make many requests quickly
        for (int i = 0; i < 110; i++) {
            given()
                    .spec(requestSpec)
                    .body(credentials)
                    .when()
                    .post("/api/auth/login");
        }

        // Next request should be rate limited
        given()
                .spec(requestSpec)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(429);
    }
}
