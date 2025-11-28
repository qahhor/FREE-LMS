package com.freelms.authoring.service;

import com.freelms.authoring.entity.InteractiveContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * Service for SCORM package operations.
 */
@Service
public class ScormService {

    private static final Logger log = LoggerFactory.getLogger(ScormService.class);

    @Value("${authoring.storage.path:/data/authoring}")
    private String storagePath;

    @Value("${authoring.base.url:http://localhost:8103}")
    private String baseUrl;

    /**
     * Parse SCORM package and extract metadata.
     */
    public ScormMetadata parseScormPackage(MultipartFile file) throws Exception {
        ScormMetadata metadata = new ScormMetadata();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equalsIgnoreCase("imsmanifest.xml")) {
                    Document doc = parseXml(zis);
                    metadata = parseManifest(doc);
                    break;
                }
                zis.closeEntry();
            }
        }

        return metadata;
    }

    /**
     * Store SCORM package.
     */
    public String storeScormPackage(MultipartFile file, UUID contentId) throws Exception {
        Path contentDir = Path.of(storagePath, "scorm", contentId.toString());
        Files.createDirectories(contentDir);

        // Extract package
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = contentDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }

        return contentDir.toString();
    }

    /**
     * Generate SCORM launch URL.
     */
    public String generateLaunchUrl(InteractiveContent content, Long userId) {
        // In real implementation, this would create a launch session and return
        // a URL that initializes the SCORM API
        String launchPage = getLaunchPage(content.getStoragePath());
        return String.format("%s/api/v1/authoring/scorm/%s/launch?userId=%d&launchPage=%s",
                baseUrl, content.getId(), userId, launchPage);
    }

    /**
     * Initialize SCORM session.
     */
    public Map<String, Object> initializeSession(UUID contentId, Long userId) {
        Map<String, Object> session = new HashMap<>();
        session.put("sessionId", UUID.randomUUID().toString());
        session.put("contentId", contentId.toString());
        session.put("userId", userId);
        session.put("startTime", System.currentTimeMillis());
        session.put("cmi", getDefaultCmiData());
        return session;
    }

    /**
     * Process SCORM API call.
     */
    public Map<String, Object> processApiCall(String sessionId, String method, Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();

        switch (method) {
            case "Initialize" -> {
                result.put("return_value", "true");
                result.put("error_code", "0");
            }
            case "GetValue" -> {
                String element = params.get("element");
                result.put("return_value", getCmiValue(element));
                result.put("error_code", "0");
            }
            case "SetValue" -> {
                String element = params.get("element");
                String value = params.get("value");
                setCmiValue(element, value);
                result.put("return_value", "true");
                result.put("error_code", "0");
            }
            case "Commit" -> {
                // Save current state
                result.put("return_value", "true");
                result.put("error_code", "0");
            }
            case "Terminate" -> {
                // End session
                result.put("return_value", "true");
                result.put("error_code", "0");
            }
            default -> {
                result.put("return_value", "false");
                result.put("error_code", "401");
            }
        }

        return result;
    }

    private Document parseXml(InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }

    private ScormMetadata parseManifest(Document doc) {
        ScormMetadata metadata = new ScormMetadata();

        // Detect SCORM version
        Element root = doc.getDocumentElement();
        String schemaVersion = root.getAttribute("version");
        if (schemaVersion.isEmpty()) {
            NodeList schemaNodes = doc.getElementsByTagName("schemaversion");
            if (schemaNodes.getLength() > 0) {
                schemaVersion = schemaNodes.item(0).getTextContent();
            }
        }
        metadata.setVersion(detectScormVersion(schemaVersion));

        // Get title
        NodeList titleNodes = doc.getElementsByTagName("title");
        if (titleNodes.getLength() > 0) {
            NodeList langStrings = ((Element)titleNodes.item(0)).getElementsByTagName("langstring");
            if (langStrings.getLength() > 0) {
                metadata.setTitle(langStrings.item(0).getTextContent());
            } else {
                metadata.setTitle(titleNodes.item(0).getTextContent());
            }
        }

        // Get description
        NodeList descNodes = doc.getElementsByTagName("description");
        if (descNodes.getLength() > 0) {
            NodeList langStrings = ((Element)descNodes.item(0)).getElementsByTagName("langstring");
            if (langStrings.getLength() > 0) {
                metadata.setDescription(langStrings.item(0).getTextContent());
            }
        }

        // Get launch page
        NodeList resourceNodes = doc.getElementsByTagName("resource");
        for (int i = 0; i < resourceNodes.getLength(); i++) {
            Element resource = (Element) resourceNodes.item(i);
            String type = resource.getAttribute("type");
            if (type != null && type.contains("sco")) {
                metadata.setLaunchPage(resource.getAttribute("href"));
                break;
            }
        }

        // Get mastery score if available
        NodeList objectiveNodes = doc.getElementsByTagName("minNormalizedMeasure");
        if (objectiveNodes.getLength() > 0) {
            try {
                double score = Double.parseDouble(objectiveNodes.item(0).getTextContent());
                metadata.setMasteryScore((int)(score * 100));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        return metadata;
    }

    private String detectScormVersion(String schemaVersion) {
        if (schemaVersion == null) return "1.2";
        if (schemaVersion.contains("2004")) {
            if (schemaVersion.contains("4th")) return "2004_4th";
            if (schemaVersion.contains("3rd")) return "2004_3rd";
            return "2004";
        }
        return "1.2";
    }

    private String getLaunchPage(String contentPath) {
        try {
            Path manifestPath = Path.of(contentPath, "imsmanifest.xml");
            if (Files.exists(manifestPath)) {
                Document doc = parseXml(new FileInputStream(manifestPath.toFile()));
                NodeList resourceNodes = doc.getElementsByTagName("resource");
                for (int i = 0; i < resourceNodes.getLength(); i++) {
                    Element resource = (Element) resourceNodes.item(i);
                    String href = resource.getAttribute("href");
                    if (href != null && !href.isEmpty()) {
                        return href;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get launch page: {}", e.getMessage());
        }
        return "index.html";
    }

    private Map<String, Object> getDefaultCmiData() {
        Map<String, Object> cmi = new HashMap<>();
        cmi.put("cmi.completion_status", "incomplete");
        cmi.put("cmi.success_status", "unknown");
        cmi.put("cmi.score.raw", "");
        cmi.put("cmi.score.min", "0");
        cmi.put("cmi.score.max", "100");
        cmi.put("cmi.location", "");
        cmi.put("cmi.suspend_data", "");
        cmi.put("cmi.total_time", "PT0H0M0S");
        return cmi;
    }

    private String getCmiValue(String element) {
        // In real implementation, this would fetch from session storage
        return "";
    }

    private void setCmiValue(String element, String value) {
        // In real implementation, this would update session storage
        log.debug("Setting SCORM value: {} = {}", element, value);
    }

    public static class ScormMetadata {
        private String title;
        private String description;
        private String version;
        private String launchPage;
        private Integer masteryScore;
        private List<String> organizations = new ArrayList<>();

        public String toJson() {
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(this);
            } catch (Exception e) {
                return "{}";
            }
        }

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getLaunchPage() { return launchPage; }
        public void setLaunchPage(String launchPage) { this.launchPage = launchPage; }
        public Integer getMasteryScore() { return masteryScore; }
        public void setMasteryScore(Integer masteryScore) { this.masteryScore = masteryScore; }
        public List<String> getOrganizations() { return organizations; }
        public void setOrganizations(List<String> organizations) { this.organizations = organizations; }
    }
}
