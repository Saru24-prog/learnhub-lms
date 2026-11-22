package com.example.learnhub.repository;

import com.example.learnhub.entity.Course;
import com.example.learnhub.entity.Enrollment;
import com.example.learnhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentOrderByEnrolledAtDesc(User student);
    List<Enrollment> findByCourseOrderByEnrolledAtDesc(Course course);
    Optional<Enrollment> findByStudentAndCourse(User student, Course course);
    boolean existsByStudentAndCourse(User student, Course course);
    long countByCourse(Course course);
    void deleteByCourse(Course course);
}
