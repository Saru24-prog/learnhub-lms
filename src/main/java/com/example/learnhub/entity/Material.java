package com.example.learnhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// A file (PDF, slides...) or a link (YouTube, Drive...) inside a section
@Entity
@Table(name = "materials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialType type;

    @Column(length = 1000)
    private String url;             // for LINK

    private String fileName;        // original name, e.g. "Lecture 1.pdf"
    private String storedName;      // unique name saved on disk
    private Long fileSize;
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
