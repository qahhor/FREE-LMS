package com.freelms.tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * FREE LMS - Base Integration Test Class
 *
 * Provides common setup and utilities for all integration tests.
 */
public abstract class BaseIntegrationTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseIntegrationTest.class);

    protected static String BASE_URL;
    protected static String AUTH_TOKEN;
    protected static String ADMIN_TOKEN;

    protected RequestSpecification requestSpec;

    // Test user credentials
    protected static final String TEST_USER_EMAIL = "test.user@freelms.com";
    protected static final String TEST_USER_PASSWORD = "TestPassword123!";
    protected static final String ADMIN_EMAIL = "admin@freelms.com";
    protected static final String ADMIN_PASSWORD = "AdminPassword123!";

    @BeforeAll
    static void setupBase() {
        BASE_URL = System.getProperty("BASE_URL", System.getenv().getOrDefault("BASE_URL", "http://localhost:8080"));
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        log.info("Integration tests configured with BASE_URL: {}", BASE_URL);
    }

    @BeforeEach
    void setupRequest() {
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }

    /**
     * Authenticates a user and returns JWT token
     */
    protected String authenticate(String email, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        return RestAssured.given()
                .spec(requestSpec)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    /**
     * Gets authenticated request specification
     */
    protected RequestSpecification authenticatedRequest() {
        if (AUTH_TOKEN == null) {
            AUTH_TOKEN = authenticate(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        }
        return RestAssured.given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + AUTH_TOKEN);
    }

    /**
     * Gets admin authenticated request specification
     */
    protected RequestSpecification adminRequest() {
        if (ADMIN_TOKEN == null) {
            ADMIN_TOKEN = authenticate(ADMIN_EMAIL, ADMIN_PASSWORD);
        }
        return RestAssured.given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + ADMIN_TOKEN);
    }

    /**
     * Generates unique test email
     */
    protected String generateTestEmail() {
        return "test." + System.currentTimeMillis() + "@freelms.com";
    }
}
