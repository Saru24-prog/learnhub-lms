package com.example.learnhub.dto;

import java.time.LocalDateTime;

public record MaterialResponse(Long id, String title, String type, String url,
                               String fileName, Long fileSize, LocalDateTime createdAt) {}
