package com.example.learnhub.repository;

import com.example.learnhub.entity.Course;
import com.example.learnhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCode(String code);
    List<Course> findByLecturerOrderByCodeAsc(User lecturer);
    List<Course> findAllByOrderByCodeAsc();
}
