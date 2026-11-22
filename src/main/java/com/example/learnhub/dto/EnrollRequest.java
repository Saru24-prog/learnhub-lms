package com.example.learnhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EnrollRequest {
    @NotBlank(message = "Enrollment key is required")
    private String enrollmentKey;
}
