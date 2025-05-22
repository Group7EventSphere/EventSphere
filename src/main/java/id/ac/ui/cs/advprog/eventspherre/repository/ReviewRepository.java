package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Method to find reviews by event ID (original integer ID)
    List<Review> findByEventId(Long eventId);

    // Method to find reviews by event UUID stored as string
    List<Review> findByEventUuid(String eventUuid);
}
