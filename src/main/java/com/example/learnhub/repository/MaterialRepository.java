package com.example.learnhub.repository;

import com.example.learnhub.entity.Material;
import com.example.learnhub.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findBySectionOrderByIdAsc(Section section);
}
