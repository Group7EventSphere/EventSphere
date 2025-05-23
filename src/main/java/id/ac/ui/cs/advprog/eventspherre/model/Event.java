package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date")
    private String eventDate;

    @Column
    private String location;

    @Column(name = "organizer_id")
    private Integer organizerId;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private Map<String, Object> details;

    public Event(Integer id, Map<String, Object> details) {
        this.id = id;
        this.details = details;

        // Extract common fields from details
        if (details != null) {
            this.title = (String) details.getOrDefault("title", "");
            this.description = (String) details.getOrDefault("description", "");
            this.eventDate = (String) details.getOrDefault("date", "");
            this.location = (String) details.getOrDefault("location", "");

            // Handle organizerId if present
            Object orgId = details.get("organizerId");
            if (orgId != null) {
                if (orgId instanceof Integer) {
                    this.organizerId = (Integer) orgId;
                } else if (orgId instanceof String) {
                    this.organizerId = Integer.parseInt((String) orgId);
                }
            }
        }

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}

