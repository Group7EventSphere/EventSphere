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
import java.util.Locale;
import java.util.UUID;

@Service
public class FileSystemImageStorageService implements ImageStorageService {

    private final Path root;

    @Autowired
    public FileSystemImageStorageService(
            @Value("${ads.storage.location:ads-images}") String storageLocation
    ) {
        this.root = Paths.get(storageLocation).toAbsolutePath().normalize();
    }

    // for testing
    public FileSystemImageStorageService(Path root) {
        this.root = root.toAbsolutePath().normalize();
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

        // Extract and whitelist extension only
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null) {
            int dot = original.lastIndexOf('.');
            if (dot > 0 && dot < original.length() - 1) {
                String rawExt = original.substring(dot).toLowerCase(Locale.ROOT);
                if (".png".equals(rawExt) || ".jpg".equals(rawExt) || ".jpeg".equals(rawExt)) {
                    ext = rawExt;
                } else {
                    throw new IllegalArgumentException("Only .png, .jpg or .jpeg extensions are allowed");
                }
            }
        }

        // Build a safe filename
        String safeName = System.currentTimeMillis()
                + "-" + UUID.randomUUID().toString()
                + ext;

        // Resolve against root and normalize
        Path target = root.resolve(safeName).normalize();

        // Ensure the target is still within the root directory
        if (!target.startsWith(root)) {
            throw new StorageException("Cannot store file outside of storage directory");
        }

        // Copy bytes
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            return safeName;
        } catch (IOException e) {
            throw new StorageException("Failed to store image", e);
        }
    }

    @Override
    public void delete(String imageName) {
        // Only delete under our root
        Path target = root.resolve(imageName).normalize();
        if (!target.startsWith(root)) {
            throw new StorageException("Cannot delete file outside of storage directory");
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            // non‐critical, just swallow
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
