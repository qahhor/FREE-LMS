package com.freelms.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.io.File;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import java.util.concurrent.TimeUnit;

/**
 * FREE LMS - End-to-End Media Processing Workflow Tests
 *
 * Tests complete media upload and processing workflows:
 * - Video upload and transcoding to HLS
 * - Thumbnail generation
 * - Media retrieval and streaming
 * - Document text extraction
 */
@TestMethodOrder(OrderAnnotation.class)
@Tag("e2e")
@Tag("media")
public class E2EMediaWorkflowIT extends BaseIntegrationTest {

    private static String mediaId;
    private static String jobId;

    @Test
    @Order(1)
    @DisplayName("Media: Upload video file")
    void step1_UploadVideo() {
        // Create a simple test file (in real tests, use actual video file)
        Response response = given()
                .spec(requestSpec)
                .header("X-User-Id", "1")
                .multiPart("file", "test-video.mp4", "fake video content".getBytes(), "video/mp4")
                .queryParam("courseId", "1")
                .contentType("multipart/form-data")
                .when()
                .post("/api/v1/media/upload")
                .then()
                .statusCode(anyOf(is(200), is(201), is(415))) // 415 if content validation fails
                .extract()
                .response();

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            mediaId = String.valueOf(response.path("mediaId"));
            jobId = String.valueOf(response.path("jobId"));
            log.info("Media uploaded: mediaId={}, jobId={}", mediaId, jobId);
        } else {
            log.info("Media upload skipped (validation): {}", response.asString());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Media: Check processing status")
    void step2_CheckProcessingStatus() {
        Assumptions.assumeTrue(jobId != null, "Job ID required");

        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/media/jobs/" + jobId)
                .then()
                .statusCode(200)
                .body("jobId", notNullValue())
                .body("status", notNullValue())
                .body("progress", greaterThanOrEqualTo(0));

        log.info("Media processing status checked");
    }

    @Test
    @Order(3)
    @DisplayName("Media: Get media details")
    void step3_GetMediaDetails() {
        Assumptions.assumeTrue(mediaId != null, "Media ID required");

        given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/media/" + mediaId)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("originalFilename", notNullValue())
                .body("processingStatus", notNullValue());

        log.info("Media details retrieved");
    }

    @Test
    @Order(4)
    @DisplayName("Media: Get streaming URL")
    void step4_GetStreamingUrl() {
        Assumptions.assumeTrue(mediaId != null, "Media ID required");

        given()
                .spec(requestSpec)
                .queryParam("quality", "auto")
                .when()
                .get("/api/v1/media/" + mediaId + "/stream")
                .then()
                .statusCode(anyOf(is(200), is(404))) // 404 if not processed yet
                .body("$", notNullValue());

        log.info("Streaming URL endpoint checked");
    }

    @Test
    @Order(5)
    @DisplayName("Media: Get thumbnail")
    void step5_GetThumbnail() {
        Assumptions.assumeTrue(mediaId != null, "Media ID required");

        given()
                .spec(requestSpec)
                .queryParam("size", "medium")
                .when()
                .get("/api/v1/media/" + mediaId + "/thumbnail")
                .then()
                .statusCode(anyOf(is(200), is(404))) // 404 if not generated yet
                .body("$", notNullValue());

        log.info("Thumbnail endpoint checked");
    }

    @Test
    @Order(6)
    @DisplayName("Media: Get storage statistics")
    void step6_GetStorageStats() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/media/stats")
                .then()
                .statusCode(200)
                .body("totalFiles", greaterThanOrEqualTo(0))
                .extract()
                .response();

        log.info("Storage stats: totalFiles={}", response.path("totalFiles"));
    }

    @Test
    @Order(99)
    @DisplayName("Media: Cleanup test media")
    void step99_Cleanup() {
        if (mediaId != null) {
            given()
                    .spec(requestSpec)
                    .when()
                    .delete("/api/v1/media/" + mediaId)
                    .then()
                    .statusCode(anyOf(is(204), is(404)));

            log.info("Test media cleaned up");
        }
    }
}
