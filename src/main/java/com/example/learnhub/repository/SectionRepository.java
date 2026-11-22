package com.example.learnhub.repository;

import com.example.learnhub.entity.Course;
import com.example.learnhub.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByCourseOrderByPositionAscIdAsc(Course course);
    long countByCourse(Course course);
}
