package com.freelms.lti.service;

import com.freelms.lti.entity.LtiLaunch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

/**
 * LTI Assignment and Grade Services (AGS) implementation.
 * Supports sending grades back to the platform.
 */
@Service
public class LtiAgsService {

    private static final Logger log = LoggerFactory.getLogger(LtiAgsService.class);

    private final RestTemplate restTemplate;

    public LtiAgsService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Create a line item (gradebook column) in the platform.
     */
    public Map<String, Object> createLineItem(
            String lineItemsUrl,
            String accessToken,
            String label,
            Double scoreMaximum,
            String resourceLinkId,
            String tag) {

        Map<String, Object> lineItem = new HashMap<>();
        lineItem.put("label", label);
        lineItem.put("scoreMaximum", scoreMaximum);
        lineItem.put("resourceLinkId", resourceLinkId);
        if (tag != null) {
            lineItem.put("tag", tag);
        }

        HttpHeaders headers = createHeaders(accessToken, "application/vnd.ims.lis.v2.lineitem+json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(lineItem, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    lineItemsUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to create line item: {}", e.getMessage());
            throw new RuntimeException("Failed to create line item", e);
        }
    }

    /**
     * Get all line items from the platform.
     */
    public List<Map<String, Object>> getLineItems(String lineItemsUrl, String accessToken) {
        HttpHeaders headers = createHeaders(accessToken, "application/vnd.ims.lis.v2.lineitemcontainer+json");
        HttpEntity<?> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    lineItemsUrl,
                    HttpMethod.GET,
                    request,
                    List.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get line items: {}", e.getMessage());
            throw new RuntimeException("Failed to get line items", e);
        }
    }

    /**
     * Post a score to the platform.
     */
    public void postScore(
            String scoresUrl,
            String accessToken,
            String userId,
            Double score,
            Double scoreMaximum,
            String activityProgress,
            String gradingProgress,
            String comment) {

        Map<String, Object> scorePayload = new HashMap<>();
        scorePayload.put("userId", userId);
        scorePayload.put("timestamp", Instant.now().toString());
        scorePayload.put("activityProgress", activityProgress != null ? activityProgress : "Completed");
        scorePayload.put("gradingProgress", gradingProgress != null ? gradingProgress : "FullyGraded");

        if (score != null) {
            scorePayload.put("scoreGiven", score);
            scorePayload.put("scoreMaximum", scoreMaximum != null ? scoreMaximum : 100.0);
        }

        if (comment != null) {
            scorePayload.put("comment", comment);
        }

        HttpHeaders headers = createHeaders(accessToken, "application/vnd.ims.lis.v1.score+json");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(scorePayload, headers);

        try {
            restTemplate.exchange(scoresUrl, HttpMethod.POST, request, Void.class);
            log.info("Posted score {} for user {} to {}", score, userId, scoresUrl);
        } catch (Exception e) {
            log.error("Failed to post score: {}", e.getMessage());
            throw new RuntimeException("Failed to post score", e);
        }
    }

    /**
     * Get results (grades) from a line item.
     */
    public List<Map<String, Object>> getResults(String lineItemUrl, String accessToken) {
        String resultsUrl = lineItemUrl + "/results";

        HttpHeaders headers = createHeaders(accessToken, "application/vnd.ims.lis.v2.resultcontainer+json");
        HttpEntity<?> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    resultsUrl,
                    HttpMethod.GET,
                    request,
                    List.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get results: {}", e.getMessage());
            throw new RuntimeException("Failed to get results", e);
        }
    }

    /**
     * Post grade using launch session context.
     */
    public void postGradeFromLaunch(
            LtiLaunch launch,
            String accessToken,
            Double score,
            Double maxScore,
            String comment) {

        String scoresUrl = launch.getScoresUrl();
        if (scoresUrl == null && launch.getLineItemUrl() != null) {
            scoresUrl = launch.getLineItemUrl() + "/scores";
        }

        if (scoresUrl == null) {
            throw new RuntimeException("No scores URL available for this launch");
        }

        postScore(
                scoresUrl,
                accessToken,
                launch.getLtiUserId(),
                score,
                maxScore,
                "Completed",
                "FullyGraded",
                comment
        );
    }

    private HttpHeaders createHeaders(String accessToken, String accept) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", accept);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
