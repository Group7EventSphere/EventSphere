package id.ac.ui.cs.advprog.eventspherre.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
        this.root = Paths.get(storageLocation)
                .toAbsolutePath()
                .normalize();
    }

    // for tests
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

        String ext;
        switch (file.getContentType()) {
            case "image/png":  ext = ".png"; break;
            case "image/jpeg": ext = ".jpg"; break;
            default:
                throw new IllegalArgumentException("Unsupported image type");
        }

        String safeName = System.currentTimeMillis()
                + "-" + UUID.randomUUID()
                + ext;

        try {
            File rootDir = root.toFile().getCanonicalFile();
            File dest = new File(rootDir, safeName).getCanonicalFile();

            String rootPath = rootDir.getPath() + File.separator;
            if (!dest.getPath().startsWith(rootPath)) {
                throw new StorageException("Cannot store file outside of storage directory");
            }

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            return safeName;

        } catch (IOException e) {
            throw new StorageException("Failed to store image", e);
        }
    }

    @Override
    public void delete(String imageName) {
        try {
            File rootDir = root.toFile().getCanonicalFile();
            File toDelete = new File(rootDir, imageName).getCanonicalFile();

            String rootPath = rootDir.getPath() + File.separator;
            if (!toDelete.getPath().startsWith(rootPath)) {
                throw new StorageException("Cannot delete file outside of storage directory");
            }

            Files.deleteIfExists(toDelete.toPath());
        } catch (IOException e) {
            // swallow; deletion failures are non-critical
        }
    }

    private void validate(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null || !(ct.equals("image/png") || ct.equals("image/jpeg"))) {
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
        public StorageException(String msg)                 { super(msg); }
        public StorageException(String msg, Throwable cause){ super(msg, cause); }
    }
}
