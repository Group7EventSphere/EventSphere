package id.ac.ui.cs.advprog.eventspherre.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileSystemImageStorageService implements ImageStorageService {

    private final Path root;

    @Autowired
    public FileSystemImageStorageService(
            @Value("${ads.storage.location:ads-images}") String storageLocation
    ) {
        // canonicalize base dir once
        this.root = Paths.get(storageLocation)
                .toAbsolutePath()
                .normalize();
    }

    // used by your FileSystemImageStorageServiceTest
    public FileSystemImageStorageService(Path testRoot) {
        this.root = testRoot.toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new StorageException("Could not create storage directory", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        validate(file);

        // Map content-type → safe extension, no user input
        String ext = "image/png".equals(file.getContentType()) ? ".png" : ".jpg";

        // Build a non-guessable filename
        String safeName = System.currentTimeMillis()
                + "-"
                + UUID.randomUUID()
                + ext;

        // Resolve & normalize (strips any “../”)
        Path target = root.resolve(safeName).normalize();

        // Double-check we didn’t break out of the storage folder
        if (!target.startsWith(root)) {
            throw new StorageException("Cannot store file outside of storage directory");
        }

        // Copy the bytes and return the **absolute** path so your tests pass
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new StorageException("Failed to store image", e);
        }
    }

    @Override
    public void delete(String imageRef) {
        // Accept either the absolute path (what store() returned)
        // or a simple filename—we’ll handle both.
        Path candidate = Paths.get(imageRef).normalize();
        Path target = candidate.isAbsolute()
                ? candidate
                : root.resolve(candidate).normalize();

        if (!target.startsWith(root)) {
            throw new StorageException("Cannot delete file outside of storage directory");
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // deletion failures are non-critical
        }
    }

    private void validate(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null
                || (!ct.equals("image/png") && !ct.equals("image/jpeg"))) {
            throw new IllegalArgumentException("Only PNG or JPEG images are allowed");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }
        if (file.getSize() > 1_000_000) {
            throw new IllegalArgumentException("File too large");
        }
    }

    public static class StorageException extends RuntimeException {
        public StorageException(String message) {
            super(message);
        }
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}