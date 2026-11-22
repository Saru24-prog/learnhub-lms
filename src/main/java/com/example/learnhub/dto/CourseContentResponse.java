package com.example.learnhub.dto;

import java.util.List;

// canManage = true for admin / course lecturer (they see edit buttons)
public record CourseContentResponse(CourseResponse course, List<SectionResponse> sections, boolean canManage) {}
