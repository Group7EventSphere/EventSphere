package id.ac.ui.cs.advprog.eventspherre.dto;

import org.springframework.web.multipart.MultipartFile;

public class CreateAdDto {
    private String title;
    private String description;
    private MultipartFile image;

    // --- getters & setters ---
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
}
