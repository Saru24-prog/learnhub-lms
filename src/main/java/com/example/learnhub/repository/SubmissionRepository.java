package com.example.learnhub.repository;

import com.example.learnhub.entity.Assignment;
import com.example.learnhub.entity.Submission;
import com.example.learnhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentOrderBySubmittedAtDesc(Assignment assignment);
    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);
    long countByAssignment(Assignment assignment);
    long countByAssignmentAndMarksIsNotNull(Assignment assignment);
}
