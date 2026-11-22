package com.example.learnhub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LinkRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Link is required")
    @Pattern(regexp = "^https?://.+", message = "Link must start with http:// or https://")
    private String url;
}
