package id.ac.ui.cs.advprog.eventspherre.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileSystemImageStorageService implements ImageStorageService {

    private final Path root;
    public FileSystemImageStorageService() {
        this.root = Paths.get("ads-images");
    }

    public FileSystemImageStorageService(Path root) {
        this.root = root;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        validate(file);
        String filename = System.currentTimeMillis()
                + "-"
                + StringUtils.cleanPath(file.getOriginalFilename());
        Path target = root.resolve(filename);
        try {
            Files.copy(file.getInputStream(), target,
                    StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }

    @Override
    public void delete(String imageUrl) {
        try {
            Files.deleteIfExists(Paths.get(imageUrl));
        } catch (IOException ignored) {
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }
        if (file.getSize() > 1_000_000) {
            throw new IllegalArgumentException("File too large");
        }
        String ct = file.getContentType();
        if (ct == null || !(ct.equals("image/png") || ct.equals("image/jpeg"))) {
            throw new IllegalArgumentException("Only PNG or JPEG images are allowed");
        }
    }
}
