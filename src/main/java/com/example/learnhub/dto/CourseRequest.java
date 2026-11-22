package com.example.learnhub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CourseRequest {

    @NotBlank(message = "Course code is required")
    @Size(max = 20, message = "Course code is too long")
    private String code;

    @NotBlank(message = "Course title is required")
    private String title;

    @Size(max = 1000, message = "Description is too long")
    private String description;

    @NotBlank(message = "Enrollment key is required")
    @Size(min = 4, message = "Enrollment key must be at least 4 characters")
    private String enrollmentKey;
}
