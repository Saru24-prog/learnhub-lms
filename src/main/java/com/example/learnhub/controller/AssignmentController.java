package com.example.learnhub.controller;

import com.example.learnhub.dto.*;
import com.example.learnhub.entity.Submission;
import com.example.learnhub.service.AssignmentService;
import com.example.learnhub.storage.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final FileStorageService storage;

    // ----- assignments -----
    @GetMapping("/courses/{courseId}/assignments")
    public List<AssignmentResponse> list(Authentication auth, @PathVariable Long courseId) {
        return assignmentService.list(auth.getName(), courseId);
    }

    @PostMapping("/courses/{courseId}/assignments")
    public ResponseEntity<AssignmentResponse> create(Authentication auth, @PathVariable Long courseId,
                                                     @Valid @RequestBody AssignmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.create(auth.getName(), courseId, req));
    }

    @PutMapping("/assignments/{id}")
    public AssignmentResponse update(Authentication auth, @PathVariable Long id,
                                     @Valid @RequestBody AssignmentRequest req) {
        return assignmentService.update(auth.getName(), id, req);
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        assignmentService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // ----- submissions -----
    @GetMapping("/assignments/{id}/submissions")
    public List<SubmissionResponse> submissions(Authentication auth, @PathVariable Long id) {
        return assignmentService.submissions(auth.getName(), id);
    }

    @PostMapping(value = "/assignments/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SubmissionResponse submit(Authentication auth, @PathVariable Long id,
                                     @RequestParam(required = false) String comment,
                                     @RequestParam("file") MultipartFile file) {
        return assignmentService.submit(auth.getName(), id, comment, file);
    }

    @PutMapping("/submissions/{id}/grade")
    public SubmissionResponse grade(Authentication auth, @PathVariable Long id,
                                    @Valid @RequestBody GradeRequest req) {
        return assignmentService.grade(auth.getName(), id, req);
    }

    @GetMapping("/submissions/{id}/download")
    public ResponseEntity<Resource> download(Authentication auth, @PathVariable Long id) {
        Submission s = assignmentService.getSubmissionFile(auth.getName(), id);
        Resource file = storage.load(s.getStoredName());

        MediaType type;
        try {
            type = s.getContentType() != null ? MediaType.parseMediaType(s.getContentType()) : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(s.getFileName(), StandardCharsets.UTF_8).build().toString())
                .body(file);
    }
}
