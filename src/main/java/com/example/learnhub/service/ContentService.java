package com.example.learnhub.service;

import com.example.learnhub.dto.*;
import com.example.learnhub.entity.*;
import com.example.learnhub.exception.*;
import com.example.learnhub.repository.*;
import com.example.learnhub.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final MaterialRepository materialRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService storage;

    // ================= VIEW =================

    // Admin, the course lecturer, and enrolled students can view the content
    public CourseContentResponse getContent(String email, Long courseId) {
        User user = findUser(email);
        Course course = findCourse(courseId);
        boolean manage = canManage(user, course);
        if (!manage && !enrollmentRepository.existsByStudentAndCourse(user, course)) {
            throw new AccessDeniedException("You are not enrolled in this course");
        }

        List<SectionResponse> sections = sectionRepository.findByCourseOrderByPositionAscIdAsc(course)
                .stream().map(this::toSection).toList();

        CourseResponse info = new CourseResponse(
                course.getId(), course.getCode(), course.getTitle(), course.getDescription(),
                course.getLecturer().getFullName(), enrollmentRepository.countByCourse(course),
                manage ? course.getEnrollmentKey() : null, !manage, course.getCreatedAt());

        return new CourseContentResponse(info, sections, manage);
    }

    // ================= SECTIONS =================

    public SectionResponse addSection(String email, Long courseId, SectionRequest req) {
        Course course = findCourse(courseId);
        checkManage(email, course);
        Section section = Section.builder()
                .title(req.getTitle().trim())
                .description(req.getDescription())
                .position((int) sectionRepository.countByCourse(course) + 1)
                .course(course)
                .build();
        return toSection(sectionRepository.save(section));
    }

    public SectionResponse updateSection(String email, Long sectionId, SectionRequest req) {
        Section section = findSection(sectionId);
        checkManage(email, section.getCourse());
        section.setTitle(req.getTitle().trim());
        section.setDescription(req.getDescription());
        return toSection(sectionRepository.save(section));
    }

    public void deleteSection(String email, Long sectionId) {
        Section section = findSection(sectionId);
        checkManage(email, section.getCourse());
        deleteSectionAndFiles(section);
    }

    // ================= MATERIALS =================

    public MaterialResponse addFile(String email, Long sectionId, String title, MultipartFile file) {
        Section section = findSection(sectionId);
        checkManage(email, section.getCourse());
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose a file");
        }
        String original = StringUtils.getFilename(StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()));
        String storedName = storage.store(file);

        Material material = Material.builder()
                .title(title == null || title.isBlank() ? original : title.trim())
                .type(MaterialType.FILE)
                .fileName(original)
                .storedName(storedName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .section(section)
                .build();
        return toMaterial(materialRepository.save(material));
    }

    public MaterialResponse addLink(String email, Long sectionId, LinkRequest req) {
        Section section = findSection(sectionId);
        checkManage(email, section.getCourse());
        Material material = Material.builder()
                .title(req.getTitle().trim())
                .type(MaterialType.LINK)
                .url(req.getUrl().trim())
                .section(section)
                .build();
        return toMaterial(materialRepository.save(material));
    }

    public void deleteMaterial(String email, Long materialId) {
        Material material = findMaterial(materialId);
        checkManage(email, material.getSection().getCourse());
        if (material.getType() == MaterialType.FILE) storage.delete(material.getStoredName());
        materialRepository.delete(material);
    }

    // Returns the material if this user is allowed to download it
    @Transactional(readOnly = true)
    public Material getDownloadable(String email, Long materialId) {
        User user = findUser(email);
        Material material = findMaterial(materialId);
        Course course = material.getSection().getCourse();
        if (!canManage(user, course) && !enrollmentRepository.existsByStudentAndCourse(user, course)) {
            throw new AccessDeniedException("You are not enrolled in this course");
        }
        if (material.getType() != MaterialType.FILE) {
            throw new BadRequestException("This material is a link, not a file");
        }
        return material;
    }

    // Used when a whole course is deleted
    public void deleteAllContent(Course course) {
        sectionRepository.findByCourseOrderByPositionAscIdAsc(course).forEach(this::deleteSectionAndFiles);
    }

    // ================= HELPERS =================

    private void deleteSectionAndFiles(Section section) {
        materialRepository.findBySectionOrderByIdAsc(section).forEach(m -> {
            if (m.getType() == MaterialType.FILE) storage.delete(m.getStoredName());
            materialRepository.delete(m);
        });
        sectionRepository.delete(section);
    }

    private boolean canManage(User user, Course course) {
        return user.getRole() == Role.ADMIN
                || (user.getRole() == Role.LECTURER && course.getLecturer().getId().equals(user.getId()));
    }

    private void checkManage(String email, Course course) {
        if (!canManage(findUser(email), course)) {
            throw new AccessDeniedException("Only the course lecturer can change the content");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private Section findSection(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
    }

    private Material findMaterial(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
    }

    private SectionResponse toSection(Section s) {
        List<MaterialResponse> materials = materialRepository.findBySectionOrderByIdAsc(s)
                .stream().map(this::toMaterial).toList();
        return new SectionResponse(s.getId(), s.getTitle(), s.getDescription(), s.getPosition(), materials);
    }

    private MaterialResponse toMaterial(Material m) {
        return new MaterialResponse(m.getId(), m.getTitle(), m.getType().name(), m.getUrl(),
                m.getFileName(), m.getFileSize(), m.getCreatedAt());
    }
}
