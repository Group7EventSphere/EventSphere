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

    public Ad() { }

    public Ad(Long id, String title, String description, String imageUrl, String userRole) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userRole = userRole;
    }

    public Ad(Long id, String title, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userRole = "USER";
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
        return "Ad [id=" + id + ", title=" + title + ", description=" + description + ", imageUrl=" + imageUrl + "]";
    }
}
