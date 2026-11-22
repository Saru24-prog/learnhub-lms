package com.example.learnhub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class GradeRequest {
    @NotNull(message = "Marks are required")
    @Min(value = 0, message = "Marks cannot be negative")
    private Integer marks;

    @Size(max = 2000, message = "Feedback is too long")
    private String feedback;
}
