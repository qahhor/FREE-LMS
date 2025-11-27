package com.freelms.media.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for processing job status.
 */
public class ProcessingJobResponse {

    private UUID jobId;
    private UUID mediaId;
    private String jobType;
    private String status;
    private Integer progress;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long estimatedTimeRemainingSeconds;

    // Getters and Setters
    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getMediaId() {
        return mediaId;
    }

    public void setMediaId(UUID mediaId) {
        this.mediaId = mediaId;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getEstimatedTimeRemainingSeconds() {
        return estimatedTimeRemainingSeconds;
    }

    public void setEstimatedTimeRemainingSeconds(Long estimatedTimeRemainingSeconds) {
        this.estimatedTimeRemainingSeconds = estimatedTimeRemainingSeconds;
    }
}
