package com.example.learnhub.controller;

import com.example.learnhub.dto.CourseResponse;
import com.example.learnhub.dto.UserResponse;
import com.example.learnhub.entity.Role;
import com.example.learnhub.service.CourseService;
import com.example.learnhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;

    @GetMapping("/users")
    public List<UserResponse> allUsers() {
        return userService.getAll();
    }

    @PutMapping("/users/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @RequestParam Role role) {
        return userService.changeRole(id, role);
    }

    @GetMapping("/courses")
    public List<CourseResponse> allCourses() {
        return courseService.allCourses();
    }
}
