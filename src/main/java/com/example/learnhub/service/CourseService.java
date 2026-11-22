package com.example.learnhub.service;

import com.example.learnhub.dto.*;
import com.example.learnhub.entity.*;
import com.example.learnhub.exception.*;
import com.example.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ContentService contentService;
    private final AssignmentService assignmentService;

    // ================= LECTURER =================

    // Lecturer sees own courses. Admin sees and manages ALL courses.
    public List<CourseResponse> lecturerCourses(String email) {
        User lecturer = findUser(email);
        List<Course> courses = lecturer.getRole() == Role.ADMIN
                ? courseRepository.findAllByOrderByCodeAsc()
                : courseRepository.findByLecturerOrderByCodeAsc(lecturer);
        return courses.stream()
                .map(c -> toResponse(c, true, false))
                .toList();
    }

    public CourseResponse create(String email, CourseRequest req) {
        String code = req.getCode().trim().toUpperCase();
        if (courseRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Course code already exists: " + code);
        }
        Course course = Course.builder()
                .code(code)
                .title(req.getTitle().trim())
                .description(req.getDescription())
                .enrollmentKey(req.getEnrollmentKey().trim())
                .lecturer(findUser(email))
                .build();
        return toResponse(courseRepository.save(course), true, false);
    }

    public CourseResponse update(String email, Long id, CourseRequest req) {
        Course course = findOwnCourse(id, findUser(email));
        String code = req.getCode().trim().toUpperCase();
        if (!course.getCode().equals(code) && courseRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Course code already exists: " + code);
        }
        course.setCode(code);
        course.setTitle(req.getTitle().trim());
        course.setDescription(req.getDescription());
        course.setEnrollmentKey(req.getEnrollmentKey().trim());
        return toResponse(courseRepository.save(course), true, false);
    }

    public void delete(String email, Long id) {
        Course course = findOwnCourse(id, findUser(email));
        assignmentService.deleteAllForCourse(course);  // remove assignments and submissions
        contentService.deleteAllContent(course);       // remove weeks, files and links
        enrollmentRepository.deleteByCourse(course);   // remove enrollments
        courseRepository.delete(course);
    }

    public List<CourseStudentResponse> courseStudents(String email, Long courseId) {
        Course course = findOwnCourse(courseId, findUser(email));
        return enrollmentRepository.findByCourseOrderByEnrolledAtDesc(course).stream()
                .map(e -> new CourseStudentResponse(
                        e.getStudent().getId(),
                        e.getStudent().getFullName(),
                        e.getStudent().getEmail(),
                        e.getEnrolledAt()))
                .toList();
    }

    public void removeStudent(String email, Long courseId, Long studentId) {
        Course course = findOwnCourse(courseId, findUser(email));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new ResourceNotFoundException("Student is not enrolled in this course"));
        enrollmentRepository.delete(enrollment);
    }

    // ================= STUDENT =================

    // All courses, with "enrolled" = true for the ones this student has joined
    public List<CourseResponse> browseCourses(String email) {
        User student = findUser(email);
        Set<Long> enrolledIds = enrollmentRepository.findByStudentOrderByEnrolledAtDesc(student).stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());
        return courseRepository.findAllByOrderByCodeAsc().stream()
                .map(c -> toResponse(c, false, enrolledIds.contains(c.getId())))
                .toList();
    }

    public List<CourseResponse> studentCourses(String email) {
        User student = findUser(email);
        return enrollmentRepository.findByStudentOrderByEnrolledAtDesc(student).stream()
                .map(e -> toResponse(e.getCourse(), false, true))
                .toList();
    }

    public CourseResponse enroll(String email, Long courseId, String key) {
        User student = findUser(email);
        Course course = findCourse(courseId);
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new DuplicateResourceException("You are already enrolled in this course");
        }
        if (!course.getEnrollmentKey().equals(key.trim())) {
            throw new BadRequestException("Invalid enrollment key");
        }
        enrollmentRepository.save(Enrollment.builder().student(student).course(course).build());
        return toResponse(course, false, true);
    }

    public void unenroll(String email, Long courseId) {
        User student = findUser(email);
        Course course = findCourse(courseId);
        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new ResourceNotFoundException("You are not enrolled in this course"));
        enrollmentRepository.delete(enrollment);
    }

    // ================= ADMIN =================

    public List<CourseResponse> allCourses() {
        return courseRepository.findAllByOrderByCodeAsc().stream()
                .map(c -> toResponse(c, true, false))
                .toList();
    }

    // ================= HELPERS =================

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    // A lecturer can only manage their OWN courses. Admin can manage any course.
    private Course findOwnCourse(Long id, User lecturer) {
        Course course = findCourse(id);
        if (lecturer.getRole() != Role.ADMIN && !course.getLecturer().getId().equals(lecturer.getId())) {
            throw new AccessDeniedException("This is not your course");
        }
        return course;
    }

    private CourseResponse toResponse(Course c, boolean showKey, boolean enrolled) {
        return new CourseResponse(
                c.getId(),
                c.getCode(),
                c.getTitle(),
                c.getDescription(),
                c.getLecturer().getFullName(),
                enrollmentRepository.countByCourse(c),
                showKey ? c.getEnrollmentKey() : null,
                enrolled,
                c.getCreatedAt());
    }
}
