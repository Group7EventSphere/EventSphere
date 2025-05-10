package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;


@Entity
@Table(name = "ads")
@Getter
@Setter
@NoArgsConstructor
@Builder
public class Ad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1024)
    private String description;

    @Column(nullable = false)
    private String imageUrl;

    /**
     * Role of the creator (defaults to USER).
     */
    @Default
    @Column(nullable = false)
    private String userRole = "USER";

    @Default
    @Column(nullable = false)
    private boolean active = true;

    public Ad(Long id,
              String title,
              String description,
              String imageUrl) {
        this(id, title, description, imageUrl, "USER", true);
    }

    public Ad(Long id,
              String title,
              String description,
              String imageUrl,
              String userRole) {
        this(id, title, description, imageUrl, userRole, true);
    }

    public Ad(Long id,
              String title,
              String description,
              String imageUrl,
              String userRole,
              boolean active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userRole = userRole;
        this.active = active;
    }
}