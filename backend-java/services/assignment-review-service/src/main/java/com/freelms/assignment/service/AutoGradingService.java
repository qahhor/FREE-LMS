package com.freelms.assignment.service;

import com.freelms.assignment.entity.AssignmentSubmission;
import org.springframework.stereotype.Service;

@Service
public class AutoGradingService {

    public AutoGradeResult grade(AssignmentSubmission submission) {
        // In real implementation: AI-based grading, code execution, etc.
        AutoGradeResult result = new AutoGradeResult();
        result.setScore(0f);
        result.setFeedback("Auto-grading not configured");
        return result;
    }

    public static class AutoGradeResult {
        private Float score;
        private String feedback;

        public Float getScore() { return score; }
        public void setScore(Float score) { this.score = score; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }
}
