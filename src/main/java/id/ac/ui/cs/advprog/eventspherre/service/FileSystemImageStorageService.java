package id.ac.ui.cs.advprog.eventspherre.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;

@Service
public class FileSystemImageStorageService implements ImageStorageService {
    private final Path root = Paths.get("ads-images");

    @PostConstruct
    public void init() {
        try { Files.createDirectories(root); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    @Override
    public String store(MultipartFile file) {
        validate(file);
        String filename = System.currentTimeMillis() + "-" +
                StringUtils.cleanPath(file.getOriginalFilename());
        Path tgt = root.resolve(filename);
        try {
            Files.copy(file.getInputStream(), tgt, StandardCopyOption.REPLACE_EXISTING);
            return tgt.toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }

    @Override
    public void delete(String imageUrl) {
        try { Files.deleteIfExists(Paths.get(imageUrl)); }
        catch (IOException ignored) {}
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");
        if (file.getSize() > 1_000_000) throw new IllegalArgumentException("Too large");
        String ct = file.getContentType();
        if (!(ct.equals("image/png")||ct.equals("image/jpeg")))
            throw new IllegalArgumentException("Only PNG/JPEG");
    }
}
