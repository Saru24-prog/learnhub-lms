package com.example.learnhub.dto;

import java.time.LocalDateTime;

// enrollmentKey is only filled for the course's lecturer and admins (null for students)
public record CourseResponse(
        Long id,
        String code,
        String title,
        String description,
        String lecturerName,
        long studentCount,
        String enrollmentKey,
        boolean enrolled,
        LocalDateTime createdAt) {}
