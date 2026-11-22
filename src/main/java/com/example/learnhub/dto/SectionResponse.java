package com.example.learnhub.dto;

import java.util.List;

public record SectionResponse(Long id, String title, String description, int position,
                              List<MaterialResponse> materials) {}
