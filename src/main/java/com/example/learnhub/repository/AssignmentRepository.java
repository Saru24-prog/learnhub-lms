package com.example.learnhub.repository;

import com.example.learnhub.entity.Assignment;
import com.example.learnhub.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseOrderByDueDateAsc(Course course);
}
