package com.example.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// One student's submission for one assignment
@Entity
@Table(name = "submissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "student_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String storedName;

    private Long fileSize;
    private String contentType;

    @Column(length = 1000)
    private String comment;          // student's note

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private boolean late;            // submitted after the deadline?

    private Integer marks;           // null = not graded yet

    @Column(length = 2000)
    private String feedback;

    private LocalDateTime gradedAt;
}
