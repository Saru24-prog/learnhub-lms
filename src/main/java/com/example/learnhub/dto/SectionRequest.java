package com.example.learnhub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SectionRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title is too long")
    private String title;

    @Size(max = 2000, message = "Description is too long")
    private String description;
}
