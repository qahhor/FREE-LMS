package com.freelms.assignment.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlagiarismService {

    @Async
    public void checkPlagiarismAsync(UUID submissionId) {
        // In real implementation: integrate with Turnitin, Copyleaks, etc.
    }
}
