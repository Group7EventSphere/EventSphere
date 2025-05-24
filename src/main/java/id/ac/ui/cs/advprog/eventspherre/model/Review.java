package id.ac.ui.cs.advprog.eventspherre.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="reviews")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private int eventId;

    @Column(name = "attendee_id")
    private Long attendeeId;

    private String reviewText;
    private int rating;

    public Review() { }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="attendee_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({ "password", "roles", "otherSensitiveFields" })
    private User attendee;

    public Review(int eventId, Long attendeeId, String reviewText, int rating) {
        this.eventId = eventId;
        this.attendeeId = attendeeId;
        this.reviewText = reviewText;
        this.rating = rating;
    }
}

