package com.example.learnhub.dto;

import java.time.LocalDateTime;

// mySubmission is filled only for students (their own submission, or null)
public record AssignmentResponse(
        Long id, String title, String instructions, LocalDateTime dueDate, int maxMarks,
        LocalDateTime createdAt, long submissionCount, long gradedCount,
        SubmissionResponse mySubmission) {}
