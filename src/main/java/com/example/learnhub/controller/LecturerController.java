package com.example.learnhub.controller;

import com.example.learnhub.dto.*;
import com.example.learnhub.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Only LECTURER can access /api/lecturer/** (see SecurityConfig)
@RestController
@RequestMapping("/api/lecturer/courses")
@RequiredArgsConstructor
public class LecturerController {

    private final CourseService courseService;

    @GetMapping
    public List<CourseResponse> myCourses(Authentication auth) {
        return courseService.lecturerCourses(auth.getName());
    }

    @PostMapping
    public ResponseEntity<CourseResponse> create(Authentication auth, @Valid @RequestBody CourseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(auth.getName(), req));
    }

    @PutMapping("/{id}")
    public CourseResponse update(Authentication auth, @PathVariable Long id, @Valid @RequestBody CourseRequest req) {
        return courseService.update(auth.getName(), id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        courseService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/students")
    public List<CourseStudentResponse> students(Authentication auth, @PathVariable Long id) {
        return courseService.courseStudents(auth.getName(), id);
    }

    @DeleteMapping("/{courseId}/students/{studentId}")
    public ResponseEntity<Void> removeStudent(Authentication auth, @PathVariable Long courseId,
                                              @PathVariable Long studentId) {
        courseService.removeStudent(auth.getName(), courseId, studentId);
        return ResponseEntity.noContent().build();
    }
}
