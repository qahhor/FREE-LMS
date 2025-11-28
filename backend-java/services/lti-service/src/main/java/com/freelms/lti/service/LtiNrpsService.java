package com.freelms.lti.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * LTI Names and Roles Provisioning Services (NRPS) implementation.
 * Supports retrieving course membership from the platform.
 */
@Service
public class LtiNrpsService {

    private static final Logger log = LoggerFactory.getLogger(LtiNrpsService.class);

    private final RestTemplate restTemplate;

    public LtiNrpsService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get all members of a context (course).
     */
    public MembershipResult getContextMemberships(String membershipsUrl, String accessToken) {
        return getContextMemberships(membershipsUrl, accessToken, null, null);
    }

    /**
     * Get members with optional filtering.
     */
    public MembershipResult getContextMemberships(
            String membershipsUrl,
            String accessToken,
            String role,
            Integer limit) {

        StringBuilder url = new StringBuilder(membershipsUrl);
        List<String> params = new ArrayList<>();

        if (role != null) {
            params.add("role=" + role);
        }
        if (limit != null) {
            params.add("limit=" + limit);
        }

        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.ims.lti-nrps.v2.membershipcontainer+json");

        HttpEntity<?> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url.toString(),
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            MembershipResult result = new MembershipResult();

            // Parse members
            List<Map<String, Object>> members = (List<Map<String, Object>>) body.get("members");
            if (members != null) {
                result.setMembers(members.stream().map(this::parseMember).toList());
            }

            // Parse context
            Map<String, Object> context = (Map<String, Object>) body.get("context");
            if (context != null) {
                result.setContextId((String) context.get("id"));
                result.setContextTitle((String) context.get("title"));
            }

            // Check for pagination
            HttpHeaders responseHeaders = response.getHeaders();
            List<String> linkHeaders = responseHeaders.get("Link");
            if (linkHeaders != null) {
                for (String link : linkHeaders) {
                    if (link.contains("rel=\"next\"")) {
                        // Extract next URL
                        int start = link.indexOf('<') + 1;
                        int end = link.indexOf('>');
                        if (start > 0 && end > start) {
                            result.setNextPageUrl(link.substring(start, end));
                        }
                    }
                }
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to get memberships: {}", e.getMessage());
            throw new RuntimeException("Failed to get memberships", e);
        }
    }

    /**
     * Get all members, handling pagination automatically.
     */
    public List<Member> getAllMembers(String membershipsUrl, String accessToken) {
        List<Member> allMembers = new ArrayList<>();
        String nextUrl = membershipsUrl;

        while (nextUrl != null) {
            MembershipResult result = getContextMemberships(nextUrl, accessToken, null, null);
            if (result.getMembers() != null) {
                allMembers.addAll(result.getMembers());
            }
            nextUrl = result.getNextPageUrl();
        }

        return allMembers;
    }

    /**
     * Get members by role.
     */
    public List<Member> getMembersByRole(String membershipsUrl, String accessToken, String role) {
        MembershipResult result = getContextMemberships(membershipsUrl, accessToken, role, null);
        return result.getMembers() != null ? result.getMembers() : Collections.emptyList();
    }

    /**
     * Get instructors only.
     */
    public List<Member> getInstructors(String membershipsUrl, String accessToken) {
        return getMembersByRole(membershipsUrl, accessToken, "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor");
    }

    /**
     * Get learners only.
     */
    public List<Member> getLearners(String membershipsUrl, String accessToken) {
        return getMembersByRole(membershipsUrl, accessToken, "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner");
    }

    private Member parseMember(Map<String, Object> data) {
        Member member = new Member();
        member.setUserId((String) data.get("user_id"));
        member.setName((String) data.get("name"));
        member.setGivenName((String) data.get("given_name"));
        member.setFamilyName((String) data.get("family_name"));
        member.setEmail((String) data.get("email"));
        member.setPicture((String) data.get("picture"));

        List<String> roles = (List<String>) data.get("roles");
        if (roles != null) {
            member.setRoles(new ArrayList<>(roles));
        }

        member.setStatus((String) data.get("status"));

        Map<String, Object> message = (Map<String, Object>) data.get("message");
        if (message != null) {
            member.setLisPersonSourcedId((String) message.get("https://purl.imsglobal.org/spec/lti/claim/lis")
                    != null ? (String) ((Map) message.get("https://purl.imsglobal.org/spec/lti/claim/lis")).get("person_sourcedid") : null);
        }

        return member;
    }

    // Result classes
    public static class MembershipResult {
        private String contextId;
        private String contextTitle;
        private List<Member> members;
        private String nextPageUrl;

        public String getContextId() { return contextId; }
        public void setContextId(String contextId) { this.contextId = contextId; }

        public String getContextTitle() { return contextTitle; }
        public void setContextTitle(String contextTitle) { this.contextTitle = contextTitle; }

        public List<Member> getMembers() { return members; }
        public void setMembers(List<Member> members) { this.members = members; }

        public String getNextPageUrl() { return nextPageUrl; }
        public void setNextPageUrl(String nextPageUrl) { this.nextPageUrl = nextPageUrl; }
    }

    public static class Member {
        private String userId;
        private String name;
        private String givenName;
        private String familyName;
        private String email;
        private String picture;
        private List<String> roles;
        private String status;
        private String lisPersonSourcedId;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }

        public String getFamilyName() { return familyName; }
        public void setFamilyName(String familyName) { this.familyName = familyName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getLisPersonSourcedId() { return lisPersonSourcedId; }
        public void setLisPersonSourcedId(String lisPersonSourcedId) { this.lisPersonSourcedId = lisPersonSourcedId; }

        public boolean isInstructor() {
            return roles != null && roles.stream().anyMatch(r -> r.contains("Instructor"));
        }

        public boolean isLearner() {
            return roles != null && roles.stream().anyMatch(r -> r.contains("Learner"));
        }

        public boolean isAdmin() {
            return roles != null && roles.stream().anyMatch(r -> r.contains("Administrator"));
        }
    }
}
