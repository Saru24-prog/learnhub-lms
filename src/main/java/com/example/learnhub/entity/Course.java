package com.example.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;            // e.g. IT2030

    @Column(nullable = false)
    private String title;           // e.g. Object Oriented Programming

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String enrollmentKey;   // students need this to enroll

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;          // the lecturer who owns the course

    @CreationTimestamp
    private LocalDateTime createdAt;
}
