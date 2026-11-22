package com.example.learnhub.controller;

import com.example.learnhub.dto.*;
import com.example.learnhub.entity.Material;
import com.example.learnhub.service.ContentService;
import com.example.learnhub.storage.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

// Course content: weekly sections, files and links.
// Who can do what is checked inside ContentService.
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final FileStorageService storage;

    @GetMapping("/courses/{courseId}/content")
    public CourseContentResponse content(Authentication auth, @PathVariable Long courseId) {
        return contentService.getContent(auth.getName(), courseId);
    }

    // ----- sections -----
    @PostMapping("/courses/{courseId}/sections")
    public ResponseEntity<SectionResponse> addSection(Authentication auth, @PathVariable Long courseId,
                                                      @Valid @RequestBody SectionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.addSection(auth.getName(), courseId, req));
    }

    @PutMapping("/sections/{id}")
    public SectionResponse updateSection(Authentication auth, @PathVariable Long id,
                                         @Valid @RequestBody SectionRequest req) {
        return contentService.updateSection(auth.getName(), id, req);
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Void> deleteSection(Authentication auth, @PathVariable Long id) {
        contentService.deleteSection(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // ----- materials -----
    @PostMapping(value = "/sections/{id}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MaterialResponse> uploadFile(Authentication auth, @PathVariable Long id,
                                                       @RequestParam(required = false) String title,
                                                       @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.addFile(auth.getName(), id, title, file));
    }

    @PostMapping("/sections/{id}/links")
    public ResponseEntity<MaterialResponse> addLink(Authentication auth, @PathVariable Long id,
                                                    @Valid @RequestBody LinkRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.addLink(auth.getName(), id, req));
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<Void> deleteMaterial(Authentication auth, @PathVariable Long id) {
        contentService.deleteMaterial(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/materials/{id}/download")
    public ResponseEntity<Resource> download(Authentication auth, @PathVariable Long id) {
        Material m = contentService.getDownloadable(auth.getName(), id);
        Resource file = storage.load(m.getStoredName());

        MediaType type;
        try {
            type = m.getContentType() != null ? MediaType.parseMediaType(m.getContentType()) : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(m.getFileName(), StandardCharsets.UTF_8).build().toString())
                .body(file);
    }
}
