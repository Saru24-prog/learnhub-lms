package com.example.learnhub.dto;

import java.time.LocalDateTime;

public record SubmissionResponse(
        Long id, Long studentId, String studentName, String studentEmail,
        String fileName, Long fileSize, String comment,
        LocalDateTime submittedAt, boolean late,
        Integer marks, String feedback, LocalDateTime gradedAt) {}
