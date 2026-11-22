package com.example.learnhub.dto;

import java.time.LocalDateTime;

public record CourseStudentResponse(Long id, String fullName, String email, LocalDateTime enrolledAt) {}
