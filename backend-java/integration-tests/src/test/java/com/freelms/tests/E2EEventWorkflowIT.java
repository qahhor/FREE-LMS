package com.freelms.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * FREE LMS - End-to-End Event/Webinar Workflow Tests
 *
 * Tests complete event lifecycle:
 * - Event creation by organizer
 * - User registration
 * - Attendee management
 * - Calendar integration
 * - Event cancellation
 */
@TestMethodOrder(OrderAnnotation.class)
@Tag("e2e")
@Tag("events")
public class E2EEventWorkflowIT extends BaseIntegrationTest {

    private static String eventId;
    private static String organizerId = "1";
    private static String attendeeId = "2";
    private static String eventTitle;

    @BeforeAll
    static void setup() {
        eventTitle = "E2E Test Workshop - " + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @Order(1)
    @DisplayName("Event: Create workshop")
    void step1_CreateEvent() {
        ZonedDateTime startTime = ZonedDateTime.now().plusDays(7).withHour(10).withMinute(0);
        ZonedDateTime endTime = startTime.plusHours(2);

        Map<String, Object> event = new HashMap<>();
        event.put("title", eventTitle);
        event.put("description", "A hands-on workshop for testing event functionality");
        event.put("eventType", "WORKSHOP");
        event.put("locationType", "HYBRID");
        event.put("physicalLocation", "Conference Room A, Building 1");
        event.put("meetingPlatform", "ZOOM");
        event.put("startTime", startTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        event.put("endTime", endTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        event.put("timezone", "UTC");
        event.put("maxAttendees", 50);
        event.put("registrationRequired", true);
        event.put("allowWaitlist", true);

        Response response = given()
                .spec(requestSpec)
                .header("X-User-Id", organizerId)
                .body(event)
                .when()
                .post("/api/v1/events")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("id", notNullValue())
                .body("title", equalTo(eventTitle))
                .body("eventType", equalTo("WORKSHOP"))
                .body("status", equalTo("SCHEDULED"))
                .extract()
                .response();

        eventId = String.valueOf(response.path("id"));
        log.info("Event created: id={}, title={}", eventId, eventTitle);
    }

    @Test
    @Order(2)
    @DisplayName("Event: Get event details")
    void step2_GetEventDetails() {
        Assumptions.assumeTrue(eventId != null, "Event ID required");

        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/events/" + eventId)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("title", equalTo(eventTitle))
                .body("maxAttendees", equalTo(50))
                .body("registrationRequired", equalTo(true));

        log.info("Event details retrieved");
    }

    @Test
    @Order(3)
    @DisplayName("Event: Attendee registers for event")
    void step3_RegisterForEvent() {
        Assumptions.assumeTrue(eventId != null, "Event ID required");

        Response response = given()
                .spec(requestSpec)
                .header("X-User-Id", attendeeId)
                .when()
                .post("/api/v1/events/" + eventId + "/register")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("registrationStatus", anyOf(equalTo("REGISTERED"), equalTo("WAITLISTED")))
                .extract()
                .response();

        log.info("Attendee registered: status={}", response.path("registrationStatus"));
    }

    @Test
    @Order(4)
    @DisplayName("Event: List attendees")
    void step4_ListAttendees() {
        Assumptions.assumeTrue(eventId != null, "Event ID required");

        Response response = given()
                .spec(requestSpec)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/events/" + eventId + "/attendees")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .extract()
                .response();

        List<Map<String, Object>> attendees = response.path("content");
        log.info("Event has {} registered attendees", attendees.size());
    }

    @Test
    @Order(5)
    @DisplayName("Event: Get join URL")
    void step5_GetJoinUrl() {
        Assumptions.assumeTrue(eventId != null, "Event ID required");

        given()
                .spec(requestSpec)
                .header("X-User-Id", attendeeId)
                .when()
                .get("/api/v1/events/" + eventId + "/meeting/join")
                .then()
                .statusCode(anyOf(is(200), is(404), is(403))); // 404 if meeting not created yet

        log.info("Join URL endpoint checked");
    }

    @Test
    @Order(6)
    @DisplayName("Event: Check organizer's calendar")
    void step6_CheckOrganizerCalendar() {
        Response response = given()
                .spec(requestSpec)
                .header("X-User-Id", organizerId)
                .when()
                .get("/api/v1/events/calendar")
                .then()
                .statusCode(200)
                .body("$", notNullValue())
                .extract()
                .response();

        log.info("Organizer calendar retrieved");
    }

    @Test
    @Order(7)
    @DisplayName("Event: Check attendee's calendar")
    void step7_CheckAttendeeCalendar() {
        Response response = given()
                .spec(requestSpec)
                .header("X-User-Id", attendeeId)
                .when()
                .get("/api/v1/events/calendar")
                .then()
                .statusCode(200)
                .body("$", notNullValue())
                .extract()
                .response();

        log.info("Attendee calendar retrieved");
    }

    @Test
    @Order(8)
    @DisplayName("Event: Export iCal")
    void step8_ExportICal() {
        given()
                .spec(requestSpec)
                .header("X-User-Id", attendeeId)
                .when()
                .get("/api/v1/events/calendar/ical")
                .then()
                .statusCode(200)
                .contentType(containsString("text/calendar"));

        log.info("iCal export working");
    }

    @Test
    @Order(9)
    @DisplayName("Event: Attendee cancels registration")
    void step9_CancelRegistration() {
        Assumptions.assumeTrue(eventId != null, "Event ID required");

        given()
                .spec(requestSpec)
                .header("X-User-Id", attendeeId)
                .when()
                .delete("/api/v1/events/" + eventId + "/register")
                .then()
                .statusCode(anyOf(is(200), is(204)));

        log.info("Attendee cancelled registration");
    }

    @Test
    @Order(10)
    @DisplayName("Event: Update event details")
    void step10_UpdateEvent() {
        Assumptions.assumeTrue(eventId != null, "Event ID required");

        Map<String, Object> update = new HashMap<>();
        update.put("description", "Updated description for the workshop");
        update.put("maxAttendees", 75);

        given()
                .spec(requestSpec)
                .header("X-User-Id", organizerId)
                .body(update)
                .when()
                .put("/api/v1/events/" + eventId)
                .then()
                .statusCode(200)
                .body("maxAttendees", equalTo(75));

        log.info("Event updated");
    }

    @Test
    @Order(99)
    @DisplayName("Event: Cancel event (cleanup)")
    void step99_CancelEvent() {
        Assumptions.assumeTrue(eventId != null, "Event ID required");

        Map<String, String> reason = new HashMap<>();
        reason.put("reason", "E2E test cleanup");

        given()
                .spec(requestSpec)
                .header("X-User-Id", organizerId)
                .body(reason)
                .when()
                .post("/api/v1/events/" + eventId + "/cancel")
                .then()
                .statusCode(anyOf(is(200), is(204)))
                .body("status", equalTo("CANCELLED"));

        log.info("Event cancelled for cleanup");
    }
}
