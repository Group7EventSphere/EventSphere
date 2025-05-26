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
        this.root = Paths.get(storageLocation);
    }


    public FileSystemImageStorageService(Path root) {
        this.root = root;
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

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null) {
            int idx = original.lastIndexOf('.');
            if (idx > 0 && idx < original.length() - 1) {
                ext = original.substring(idx);
            }
        }

        String safeName = System.currentTimeMillis()
                + "-"
                + UUID.randomUUID().toString()
                + ext;

        Path target = root.resolve(safeName).normalize();

        if (!target.getParent().equals(root)) {
            throw new StorageException(
                    "Cannot store file outside of storage directory",
                     null
            );
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new StorageException("Failed to store image", e);
        }
    }

    @Override
    public void delete(String imageUrl) {
        try {
            Files.deleteIfExists(Paths.get(imageUrl));
        } catch (IOException e) {
            // Deletion failure is non‐critical—maybe the file was already gone,
        }
    }


    private void validate(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null
                || !(contentType.equals("image/png")
                || contentType.equals("image/jpeg"))) {
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
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}