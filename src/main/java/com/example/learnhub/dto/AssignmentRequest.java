package com.example.learnhub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssignmentRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title is too long")
    private String title;

    @Size(max = 4000, message = "Instructions are too long")
    private String instructions;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @NotNull(message = "Max marks is required")
    @Min(value = 1, message = "Max marks must be at least 1")
    @Max(value = 1000, message = "Max marks is too high")
    private Integer maxMarks;
}
