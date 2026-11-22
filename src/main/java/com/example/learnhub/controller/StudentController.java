package com.example.learnhub.controller;

import com.example.learnhub.dto.*;
import com.example.learnhub.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Only STUDENT can access /api/student/** (see SecurityConfig)
@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentController {

    private final CourseService courseService;

    @GetMapping
    public List<CourseResponse> browse(Authentication auth) {
        return courseService.browseCourses(auth.getName());
    }

    @GetMapping("/my")
    public List<CourseResponse> myCourses(Authentication auth) {
        return courseService.studentCourses(auth.getName());
    }

    @PostMapping("/{id}/enroll")
    public CourseResponse enroll(Authentication auth, @PathVariable Long id, @Valid @RequestBody EnrollRequest req) {
        return courseService.enroll(auth.getName(), id, req.getEnrollmentKey());
    }

    @DeleteMapping("/{id}/enroll")
    public ResponseEntity<Void> unenroll(Authentication auth, @PathVariable Long id) {
        courseService.unenroll(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
