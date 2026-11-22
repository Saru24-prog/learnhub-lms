package com.example.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String instructions;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private int maxMarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
