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

    public Ad() { }

    public Ad(Long id, String title, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Ad [id=" + id + ", title=" + title + ", description=" + description + ", imageUrl=" + imageUrl + "]";
    }
}
