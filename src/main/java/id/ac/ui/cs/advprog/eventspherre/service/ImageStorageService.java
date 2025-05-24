package id.ac.ui.cs.advprog.eventspherre.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    String store(MultipartFile file);
    void delete(String imageUrl);
}
