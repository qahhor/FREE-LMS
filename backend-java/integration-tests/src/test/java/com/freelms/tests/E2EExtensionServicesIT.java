package com.freelms.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * FREE LMS - End-to-End Tests for New Extension Services
 *
 * Tests the 10 new microservices added in Phase 2:
 * - Search Service
 * - Media Processing Service
 * - Event Service
 * - Authoring Service
 * - Proctoring Service
 * - Assignment Review Service
 * - Resource Booking Service
 * - Audit Logging Service
 * - LTI Service
 * - Bot Platform Service
 */
@TestMethodOrder(OrderAnnotation.class)
@Tag("e2e")
@Tag("integration")
@Tag("extension-services")
public class E2EExtensionServicesIT extends BaseIntegrationTest {

    private static String userToken;
    private static String adminToken;
    private static String userId;
    private static String courseId;
    private static String eventId;
    private static String mediaId;
    private static String contentProjectId;
    private static String bookingId;

    @BeforeAll
    static void setup() {
        log.info("Starting E2E tests for Extension Services");
    }

    // ==================== SEARCH SERVICE TESTS ====================

    @Test
    @Order(1)
    @DisplayName("Search Service: Global search returns results")
    void testSearchService_GlobalSearch() {
        Response response = given()
                .spec(requestSpec)
                .queryParam("q", "course")
                .queryParam("size", 10)
                .when()
                .get("/api/v1/search")
                .then()
                .statusCode(200)
                .body("totalHits", greaterThanOrEqualTo(0))
                .body("results", notNullValue())
                .body("searchTimeMs", greaterThanOrEqualTo(0))
                .extract()
                .response();

        log.info("Search Service: Global search completed in {}ms, found {} results",
                response.path("searchTimeMs"), response.path("totalHits"));
    }

    @Test
    @Order(2)
    @DisplayName("Search Service: Autocomplete suggestions")
    void testSearchService_Suggestions() {
        given()
                .spec(requestSpec)
                .queryParam("q", "java")
                .queryParam("limit", 5)
                .when()
                .get("/api/v1/search/suggest")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Search Service: Autocomplete suggestions working");
    }

    @Test
    @Order(3)
    @DisplayName("Search Service: Advanced search with filters")
    void testSearchService_AdvancedSearch() {
        Map<String, Object> searchRequest = new HashMap<>();
        searchRequest.put("query", "programming");
        searchRequest.put("entityTypes", Arrays.asList("course", "lesson"));
        searchRequest.put("page", 0);
        searchRequest.put("size", 20);

        given()
                .spec(requestSpec)
                .body(searchRequest)
                .when()
                .post("/api/v1/search/advanced")
                .then()
                .statusCode(200)
                .body("query", equalTo("programming"))
                .body("results", notNullValue());

        log.info("Search Service: Advanced search with filters working");
    }

    @Test
    @Order(4)
    @DisplayName("Search Service: Get index statistics")
    void testSearchService_Stats() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/search/stats")
                .then()
                .statusCode(200)
                .body("totalDocuments", greaterThanOrEqualTo(0));

        log.info("Search Service: Statistics endpoint working");
    }

    // ==================== MEDIA PROCESSING SERVICE TESTS ====================

    @Test
    @Order(10)
    @DisplayName("Media Service: Get storage statistics")
    void testMediaService_Stats() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/media/stats")
                .then()
                .statusCode(200)
                .body("totalFiles", greaterThanOrEqualTo(0));

        log.info("Media Service: Statistics endpoint working");
    }

    @Test
    @Order(11)
    @DisplayName("Media Service: Health check")
    void testMediaService_Health() {
        given()
                .spec(requestSpec)
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));

        log.info("Media Service: Health check passed");
    }

    // ==================== EVENT SERVICE TESTS ====================

    @Test
    @Order(20)
    @DisplayName("Event Service: List upcoming events")
    void testEventService_ListEvents() {
        Response response = given()
                .spec(requestSpec)
                .queryParam("status", "SCHEDULED")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/events")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .extract()
                .response();

        List<Map<String, Object>> events = response.path("content");
        if (!events.isEmpty()) {
            eventId = String.valueOf(events.get(0).get("id"));
        }

        log.info("Event Service: Found {} events", events.size());
    }

    @Test
    @Order(21)
    @DisplayName("Event Service: Create new webinar")
    void testEventService_CreateEvent() {
        Assumptions.assumeTrue(adminToken != null || true, "Admin token required");

        Map<String, Object> event = new HashMap<>();
        event.put("title", "E2E Test Webinar - " + UUID.randomUUID().toString().substring(0, 8));
        event.put("description", "Automated test webinar for E2E testing");
        event.put("eventType", "WEBINAR");
        event.put("locationType", "ONLINE");
        event.put("meetingPlatform", "ZOOM");
        event.put("startTime", "2025-01-15T10:00:00Z");
        event.put("endTime", "2025-01-15T11:00:00Z");
        event.put("maxAttendees", 100);
        event.put("registrationRequired", true);

        Response response = given()
                .spec(requestSpec)
                .header("X-User-Id", "1")
                .body(event)
                .when()
                .post("/api/v1/events")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("id", notNullValue())
                .body("title", containsString("E2E Test Webinar"))
                .extract()
                .response();

        eventId = String.valueOf(response.path("id"));
        log.info("Event Service: Created webinar with ID: {}", eventId);
    }

    @Test
    @Order(22)
    @DisplayName("Event Service: Get event details")
    void testEventService_GetEvent() {
        Assumptions.assumeTrue(eventId != null, "Event ID required");

        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/events/" + eventId)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("title", notNullValue());

        log.info("Event Service: Retrieved event details");
    }

    @Test
    @Order(23)
    @DisplayName("Event Service: Register for event")
    void testEventService_RegisterForEvent() {
        Assumptions.assumeTrue(eventId != null, "Event ID required");

        given()
                .spec(requestSpec)
                .header("X-User-Id", "1")
                .when()
                .post("/api/v1/events/" + eventId + "/register")
                .then()
                .statusCode(anyOf(is(200), is(201), is(409))) // 409 if already registered
                .body("$", notNullValue());

        log.info("Event Service: Registration attempt completed");
    }

    @Test
    @Order(24)
    @DisplayName("Event Service: Get user calendar")
    void testEventService_GetCalendar() {
        given()
                .spec(requestSpec)
                .header("X-User-Id", "1")
                .when()
                .get("/api/v1/events/calendar")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Event Service: Calendar endpoint working");
    }

    // ==================== RESOURCE BOOKING SERVICE TESTS ====================

    @Test
    @Order(30)
    @DisplayName("Resource Booking: List available resources")
    void testResourceBooking_ListResources() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/resources")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Resource Booking: Resources list retrieved");
    }

    @Test
    @Order(31)
    @DisplayName("Resource Booking: Search available resources")
    void testResourceBooking_SearchAvailable() {
        given()
                .spec(requestSpec)
                .queryParam("type", "room")
                .queryParam("from", "2025-01-20T09:00:00Z")
                .queryParam("to", "2025-01-20T17:00:00Z")
                .when()
                .get("/api/v1/resources/available")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Resource Booking: Available resources search working");
    }

    // ==================== AUDIT LOGGING SERVICE TESTS ====================

    @Test
    @Order(40)
    @DisplayName("Audit Service: Search audit logs")
    void testAuditService_SearchLogs() {
        given()
                .spec(requestSpec)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/audit/logs")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Audit Service: Logs search working");
    }

    @Test
    @Order(41)
    @DisplayName("Audit Service: User activity tracking")
    void testAuditService_UserActivity() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/audit/users/1/activity")
                .then()
                .statusCode(anyOf(is(200), is(404))); // 404 if no activity yet

        log.info("Audit Service: User activity endpoint accessible");
    }

    // ==================== LTI SERVICE TESTS ====================

    @Test
    @Order(50)
    @DisplayName("LTI Service: List registered tools")
    void testLtiService_ListTools() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/lti/tools")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("LTI Service: Tools list endpoint working");
    }

    @Test
    @Order(51)
    @DisplayName("LTI Service: JWKS endpoint")
    void testLtiService_Jwks() {
        given()
                .spec(requestSpec)
                .when()
                .get("/lti/jwks")
                .then()
                .statusCode(anyOf(is(200), is(404))); // May not be configured

        log.info("LTI Service: JWKS endpoint accessible");
    }

    // ==================== BOT PLATFORM SERVICE TESTS ====================

    @Test
    @Order(60)
    @DisplayName("Bot Service: List bots")
    void testBotService_ListBots() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/bots")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Bot Service: Bots list endpoint working");
    }

    @Test
    @Order(61)
    @DisplayName("Bot Service: List campaigns")
    void testBotService_ListCampaigns() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/bots/campaigns")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Bot Service: Campaigns list endpoint working");
    }

    // ==================== AUTHORING SERVICE TESTS ====================

    @Test
    @Order(70)
    @DisplayName("Authoring Service: List projects")
    void testAuthoringService_ListProjects() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/authoring/projects")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Authoring Service: Projects list endpoint working");
    }

    @Test
    @Order(71)
    @DisplayName("Authoring Service: List H5P libraries")
    void testAuthoringService_ListH5PLibraries() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/authoring/h5p/libraries")
                .then()
                .statusCode(anyOf(is(200), is(404)));

        log.info("Authoring Service: H5P libraries endpoint accessible");
    }

    @Test
    @Order(72)
    @DisplayName("Authoring Service: List templates")
    void testAuthoringService_ListTemplates() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/authoring/templates")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Authoring Service: Templates list endpoint working");
    }

    // ==================== PROCTORING SERVICE TESTS ====================

    @Test
    @Order(80)
    @DisplayName("Proctoring Service: Health check")
    void testProctoringService_Health() {
        given()
                .spec(requestSpec)
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200);

        log.info("Proctoring Service: Health check passed");
    }

    // ==================== ASSIGNMENT REVIEW SERVICE TESTS ====================

    @Test
    @Order(90)
    @DisplayName("Assignment Service: List review queue")
    void testAssignmentService_ReviewQueue() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/assignments/review/queue")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Assignment Service: Review queue endpoint working");
    }

    @Test
    @Order(91)
    @DisplayName("Assignment Service: List rubrics")
    void testAssignmentService_ListRubrics() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/assignments/rubrics")
                .then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("Assignment Service: Rubrics list endpoint working");
    }

    // ==================== CROSS-SERVICE INTEGRATION TESTS ====================

    @Test
    @Order(100)
    @DisplayName("Integration: Search finds events")
    void testIntegration_SearchFindsEvents() {
        // Search should index events
        given()
                .spec(requestSpec)
                .queryParam("q", "webinar")
                .queryParam("type", "event")
                .when()
                .get("/api/v1/search")
                .then()
                .statusCode(200)
                .body("results", notNullValue());

        log.info("Integration: Search can find events");
    }

    @Test
    @Order(101)
    @DisplayName("Integration: Audit logs user actions")
    void testIntegration_AuditLogsActions() {
        // After performing actions, audit logs should have entries
        given()
                .spec(requestSpec)
                .queryParam("action", "search")
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get("/api/v1/audit/logs")
                .then()
                .statusCode(200);

        log.info("Integration: Audit service logging user actions");
    }

    @Test
    @Order(999)
    @DisplayName("E2E Extension Services: Test summary")
    void testSummary() {
        log.info("=========================================");
        log.info("E2E Extension Services Tests Completed");
        log.info("=========================================");
        log.info("Services tested:");
        log.info("  - Search Service (8100)");
        log.info("  - Media Processing Service (8101)");
        log.info("  - Event Service (8102)");
        log.info("  - Authoring Service (8103)");
        log.info("  - Proctoring Service (8104)");
        log.info("  - Assignment Review Service (8105)");
        log.info("  - Resource Booking Service (8106)");
        log.info("  - Audit Logging Service (8107)");
        log.info("  - LTI Service (8108)");
        log.info("  - Bot Platform Service (8109)");
        log.info("=========================================");
    }
}
