package id.ac.ui.cs.advprog.eventspherre.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ad {

    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String userRole;
    private boolean isActive; // Added isActive field

    public Ad() { }

    // Constructor with 'userRole' and 'isActive' fields
    public Ad(Long id, String title, String description, String imageUrl, String userRole, boolean isActive) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userRole = userRole;
        this.isActive = isActive;
    }

    // Constructor without 'userRole' and 'isActive' (default userRole is "USER" and isActive is true)
    public Ad(Long id, String title, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userRole = "USER"; // Default to "USER" role
        this.isActive = true; // Default value for isActive
    }

    // Constructor without 'isActive' (default userRole to "USER" and isActive to true)
    public Ad(Long id, String title, String description, String imageUrl, String userRole) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userRole = userRole;
        this.isActive = true; // Default to true if not specified
    }

    public boolean canEdit() {
        return "ADMIN".equalsIgnoreCase(this.userRole);
    }

    public boolean isValid() {
        return title != null && !title.isEmpty() &&
                description != null && !description.isEmpty() &&
                imageUrl != null && imageUrl.matches(".*\\.(jpg|png)$");
    }

    @Override
    public String toString() {
        return "Ad [id=" + id + ", title=" + title + ", description=" + description + ", imageUrl=" + imageUrl + ", isActive=" + isActive + "]";
    }
}