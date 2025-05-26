package id.ac.ui.cs.advprog.eventspherre.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
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
    private Integer organizerId;    @Column(name = "is_public")
    private Boolean isPublic = null;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private Map<String, Object> details = null;

    // Field name constants
    private static final String CAPACITY_FIELD = "capacity";
    private static final String IS_PUBLIC_FIELD = "isPublic";

    public Event(Integer id, Map<String, Object> details) {
        this.id = id;
        this.details = details;

        // Extract common fields from details
        if (details != null) {
            this.title = (String) details.getOrDefault("title", "");
            this.description = (String) details.getOrDefault("description", "");
            this.eventDate = (String) details.getOrDefault("date", "");
            this.location = (String) details.getOrDefault("location", "");            // Handle organizerId if present
            Object orgId = details.get("organizerId");
            if (orgId != null) {
                if (orgId instanceof Integer integer) {
                    this.organizerId = integer;
                } else if (orgId instanceof String string) {
                    this.organizerId = Integer.parseInt(string);
                }
            }
        }

        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Integer getCapacity() {
        // If capacity field is set directly, return it
        if (capacity != null) {
            return capacity;
        }        // For backward compatibility, try to get from details map
        if (details != null && details.containsKey(CAPACITY_FIELD)) {
            Object capacityObj = details.get(CAPACITY_FIELD);
            if (capacityObj instanceof Integer integer) {
                this.capacity = integer; // Cache it in the field
                return this.capacity;
            }}
        return null;
    }

    public boolean isPublic() {
        if (isPublic != null) {
            return isPublic;
        }        // For backward compatibility, try to get from details map
        if (details != null && details.containsKey(IS_PUBLIC_FIELD)) {
            Object isPublicObj = details.get(IS_PUBLIC_FIELD);
            if (isPublicObj instanceof Boolean booleanValue) {
                this.isPublic = booleanValue; // Cache it in the field
                return this.isPublic;
            } else if (isPublicObj == null) {
                // This should throw NPE for consistency with the test expectation
                throw new NullPointerException("isPublic value in details map is null");
            }
        }
        return false; // Default value when not set
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
        // Also update the details map for backward compatibility
        if (details == null) {
            details = new HashMap<>();
        }
        details.put(CAPACITY_FIELD, capacity);
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        // Also update the details map for backward compatibility
        if (details == null) {
            details = new HashMap<>();
        }
        details.put(IS_PUBLIC_FIELD, isPublic);
    }
}
