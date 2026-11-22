package com.example.learnhub.service;

import com.example.learnhub.dto.*;
import com.example.learnhub.entity.*;
import com.example.learnhub.exception.*;
import com.example.learnhub.repository.*;
import com.example.learnhub.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService storage;

    // ================= ASSIGNMENTS =================

    public List<AssignmentResponse> list(String email, Long courseId) {
        User user = findUser(email);
        Course course = findCourse(courseId);
        boolean manage = canManage(user, course);
        if (!manage && !enrollmentRepository.existsByStudentAndCourse(user, course)) {
            throw new AccessDeniedException("You are not enrolled in this course");
        }
        return assignmentRepository.findByCourseOrderByDueDateAsc(course).stream()
                .map(a -> toResponse(a, manage ? null : user))
                .toList();
    }

    public AssignmentResponse create(String email, Long courseId, AssignmentRequest req) {
        Course course = findCourse(courseId);
        checkManage(email, course);
        Assignment a = Assignment.builder()
                .title(req.getTitle().trim())
                .instructions(req.getInstructions())
                .dueDate(req.getDueDate())
                .maxMarks(req.getMaxMarks())
                .course(course)
                .build();
        return toResponse(assignmentRepository.save(a), null);
    }

    public AssignmentResponse update(String email, Long id, AssignmentRequest req) {
        Assignment a = findAssignment(id);
        checkManage(email, a.getCourse());
        a.setTitle(req.getTitle().trim());
        a.setInstructions(req.getInstructions());
        a.setDueDate(req.getDueDate());
        a.setMaxMarks(req.getMaxMarks());
        return toResponse(assignmentRepository.save(a), null);
    }

    public void delete(String email, Long id) {
        Assignment a = findAssignment(id);
        checkManage(email, a.getCourse());
        deleteAssignmentAndFiles(a);
    }

    // ================= SUBMISSIONS =================

    public List<SubmissionResponse> submissions(String email, Long assignmentId) {
        Assignment a = findAssignment(assignmentId);
        checkManage(email, a.getCourse());
        return submissionRepository.findByAssignmentOrderBySubmittedAtDesc(a).stream()
                .map(this::toSubmission).toList();
    }

    public SubmissionResponse submit(String email, Long assignmentId, String comment, MultipartFile file) {
        User student = findUser(email);
        Assignment a = findAssignment(assignmentId);
        if (student.getRole() != Role.STUDENT || !enrollmentRepository.existsByStudentAndCourse(student, a.getCourse())) {
            throw new AccessDeniedException("Only enrolled students can submit");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose a file");
        }
        if (comment != null && comment.length() > 1000) {
            throw new BadRequestException("Comment is too long (max 1000 characters)");
        }

        Submission s = submissionRepository.findByAssignmentAndStudent(a, student).orElse(null);
        if (s != null && s.getMarks() != null) {
            throw new BadRequestException("This submission is already graded and cannot be changed");
        }

        String storedName = storage.store(file);
        if (s == null) {
            s = Submission.builder().assignment(a).student(student).build();
        } else {
            storage.delete(s.getStoredName());        // replace the old file
        }

        LocalDateTime now = LocalDateTime.now();
        s.setFileName(StringUtils.getFilename(StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())));
        s.setStoredName(storedName);
        s.setFileSize(file.getSize());
        s.setContentType(file.getContentType());
        s.setComment(comment == null || comment.isBlank() ? null : comment.trim());
        s.setSubmittedAt(now);
        s.setLate(now.isAfter(a.getDueDate()));
        return toSubmission(submissionRepository.save(s));
    }

    public SubmissionResponse grade(String email, Long submissionId, GradeRequest req) {
        Submission s = findSubmission(submissionId);
        checkManage(email, s.getAssignment().getCourse());
        int max = s.getAssignment().getMaxMarks();
        if (req.getMarks() > max) {
            throw new BadRequestException("Marks cannot be more than " + max);
        }
        s.setMarks(req.getMarks());
        s.setFeedback(req.getFeedback() == null || req.getFeedback().isBlank() ? null : req.getFeedback().trim());
        s.setGradedAt(LocalDateTime.now());
        return toSubmission(submissionRepository.save(s));
    }

    // The student who submitted, or the admin/lecturer, can download
    @Transactional(readOnly = true)
    public Submission getSubmissionFile(String email, Long submissionId) {
        User user = findUser(email);
        Submission s = findSubmission(submissionId);
        boolean owner = s.getStudent().getId().equals(user.getId());
        if (!owner && !canManage(user, s.getAssignment().getCourse())) {
            throw new AccessDeniedException("You cannot download this file");
        }
        return s;
    }

    // Used when a whole course is deleted
    public void deleteAllForCourse(Course course) {
        assignmentRepository.findByCourseOrderByDueDateAsc(course).forEach(this::deleteAssignmentAndFiles);
    }

    // ================= HELPERS =================

    private void deleteAssignmentAndFiles(Assignment a) {
        submissionRepository.findByAssignmentOrderBySubmittedAtDesc(a).forEach(s -> {
            storage.delete(s.getStoredName());
            submissionRepository.delete(s);
        });
        assignmentRepository.delete(a);
    }

    private boolean canManage(User user, Course course) {
        return user.getRole() == Role.ADMIN
                || (user.getRole() == Role.LECTURER && course.getLecturer().getId().equals(user.getId()));
    }

    private void checkManage(String email, Course course) {
        if (!canManage(findUser(email), course)) {
            throw new AccessDeniedException("Only the course lecturer can do this");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private Assignment findAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }

    private Submission findSubmission(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
    }

    private AssignmentResponse toResponse(Assignment a, User student) {
        SubmissionResponse mine = student == null ? null
                : submissionRepository.findByAssignmentAndStudent(a, student).map(this::toSubmission).orElse(null);
        return new AssignmentResponse(a.getId(), a.getTitle(), a.getInstructions(), a.getDueDate(),
                a.getMaxMarks(), a.getCreatedAt(),
                submissionRepository.countByAssignment(a),
                submissionRepository.countByAssignmentAndMarksIsNotNull(a),
                mine);
    }

    private SubmissionResponse toSubmission(Submission s) {
        return new SubmissionResponse(s.getId(), s.getStudent().getId(), s.getStudent().getFullName(),
                s.getStudent().getEmail(), s.getFileName(), s.getFileSize(), s.getComment(),
                s.getSubmittedAt(), s.isLate(), s.getMarks(), s.getFeedback(), s.getGradedAt());
    }
}
