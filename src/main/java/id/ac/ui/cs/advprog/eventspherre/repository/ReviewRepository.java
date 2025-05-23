package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Method to find reviews by event ID (original integer ID)
    List<Review> findByEventId(int eventId);

    Optional<Review> findByAttendeeIdAndEventId(Long attendeeId, int eventId);
}
