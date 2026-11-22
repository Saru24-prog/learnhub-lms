package com.example.learnhub.storage;

import com.example.learnhub.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

// Saves uploaded files in the "uploads" folder with a unique name
@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String store(MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot).replaceAll("[^A-Za-z0-9.]", "");
        String storedName = UUID.randomUUID() + ext;

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save file", e);
        }
        return storedName;
    }

    public Resource load(String storedName) {
        Path path = root.resolve(storedName).normalize();
        if (!path.startsWith(root) || !Files.exists(path)) {
            throw new ResourceNotFoundException("File not found");
        }
        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found");
        }
    }

    public void delete(String storedName) {
        if (storedName == null) return;
        try {
            Files.deleteIfExists(root.resolve(storedName).normalize());
        } catch (IOException ignored) {
            // file already gone - nothing to do
        }
    }
}
