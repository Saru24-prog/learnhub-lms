package com.example.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// One week / topic inside a course, e.g. "Week 1 - Introduction"
@Entity
@Table(name = "sections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private int position;           // order: 1, 2, 3...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
