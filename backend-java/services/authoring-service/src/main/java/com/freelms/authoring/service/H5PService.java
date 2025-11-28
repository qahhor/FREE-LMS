package com.freelms.authoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * Service for H5P content operations.
 */
@Service
public class H5PService {

    private static final Logger log = LoggerFactory.getLogger(H5PService.class);

    @Value("${authoring.storage.path:/data/authoring}")
    private String storagePath;

    private final ObjectMapper objectMapper;

    public H5PService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parse H5P package and extract metadata.
     */
    public H5PMetadata parseH5PPackage(MultipartFile file) throws Exception {
        H5PMetadata metadata = new H5PMetadata();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("h5p.json")) {
                    String content = new String(zis.readAllBytes());
                    JsonNode h5pJson = objectMapper.readTree(content);

                    metadata.setTitle(h5pJson.path("title").asText("Untitled"));
                    metadata.setContentType(h5pJson.path("mainLibrary").asText());

                    if (h5pJson.has("authors")) {
                        List<String> authors = new ArrayList<>();
                        h5pJson.path("authors").forEach(a -> authors.add(a.path("name").asText()));
                        metadata.setAuthors(authors);
                    }

                    if (h5pJson.has("license")) {
                        metadata.setLicense(h5pJson.path("license").asText());
                    }
                }

                if (entry.getName().equals("content/content.json")) {
                    String content = new String(zis.readAllBytes());
                    metadata.setContentJson(content);
                }

                zis.closeEntry();
            }
        }

        return metadata;
    }

    /**
     * Store H5P package.
     */
    public String storeH5PPackage(MultipartFile file, UUID contentId) throws Exception {
        Path contentDir = Path.of(storagePath, "h5p", contentId.toString());
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
     * Get available H5P content types.
     */
    public List<Map<String, Object>> getAvailableContentTypes() {
        // In real implementation, this would fetch from H5P Hub or local library
        return List.of(
                Map.of("id", "H5P.InteractiveVideo", "name", "Interactive Video",
                        "description", "Create videos enriched with interactions"),
                Map.of("id", "H5P.CoursePresentation", "name", "Course Presentation",
                        "description", "Create a presentation with interactive slides"),
                Map.of("id", "H5P.QuestionSet", "name", "Quiz (Question Set)",
                        "description", "Create a sequence of various question types"),
                Map.of("id", "H5P.MultiChoice", "name", "Multiple Choice",
                        "description", "Create flexible multiple choice questions"),
                Map.of("id", "H5P.DragAndDrop", "name", "Drag and Drop",
                        "description", "Create drag and drop tasks with images"),
                Map.of("id", "H5P.Flashcards", "name", "Flashcards",
                        "description", "Create stylish and modern flashcards"),
                Map.of("id", "H5P.Timeline", "name", "Timeline",
                        "description", "Create a timeline of events"),
                Map.of("id", "H5P.BranchingScenario", "name", "Branching Scenario",
                        "description", "Create adaptive scenarios for dilemma-based learning")
        );
    }

    /**
     * Generate H5P embed HTML.
     */
    public String generateEmbedHtml(UUID contentId, String contentJson) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <script src="/h5p/js/h5p.js"></script>
                <link rel="stylesheet" href="/h5p/css/h5p.css">
            </head>
            <body>
                <div class="h5p-content" data-content-id="%s">
                    <script type="application/json">%s</script>
                </div>
            </body>
            </html>
            """, contentId, contentJson);
    }

    public static class H5PMetadata {
        private String title;
        private String description;
        private String contentType;
        private String contentJson;
        private List<String> authors;
        private String license;

        public String toJson() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (Exception e) {
                return "{}";
            }
        }

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getContentJson() { return contentJson; }
        public void setContentJson(String contentJson) { this.contentJson = contentJson; }
        public List<String> getAuthors() { return authors; }
        public void setAuthors(List<String> authors) { this.authors = authors; }
        public String getLicense() { return license; }
        public void setLicense(String license) { this.license = license; }
    }
}
