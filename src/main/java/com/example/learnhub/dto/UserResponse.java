package com.example.learnhub.dto;

import com.example.learnhub.entity.User;
import java.time.LocalDateTime;

public record UserResponse(Long id, String fullName, String email, String role, LocalDateTime createdAt) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(), u.getCreatedAt());
    }
}
