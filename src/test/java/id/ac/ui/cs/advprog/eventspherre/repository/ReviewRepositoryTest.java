package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository repo;

    @Test
    void saveAndFind() {
        Review input = new Review(10L, 20L, "Test review", 4);

        Review saved = repo.save(input);

        assertNotNull(saved.getId(), "ID should be generated");
        Optional<Review> found = repo.findById(saved.getId());
        assertTrue(found.isPresent(), "Review must be retrievable");
        assertEquals("Test review", found.get().getReviewText());
    }

    @Test
    void deleteReview() {
        Review toDelete = repo.save(new Review(1L, 2L, "To delete", 3));
        Long id = toDelete.getId();

        repo.deleteById(id);

        Optional<Review> found = repo.findById(id);
        assertFalse(found.isPresent(), "Review should be gone after delete");
    }

    @Test
    void findByEventId_returnsAllMatchingReviews() {
        Review a = repo.save(new Review(100L, 10L, "First", 5));
        Review b = repo.save(new Review(100L, 11L, "Second", 4));
        repo.save(new Review(101L, 10L, "Other event", 3));

        List<Review> list = repo.findByEventId(100L);

        assertEquals(2, list.size(), "Should return exactly 2 reviews for event 100");
        assertTrue(list.stream().anyMatch(r -> r.getId().equals(a.getId())));
        assertTrue(list.stream().anyMatch(r -> r.getId().equals(b.getId())));
    }

    @Test
    void findByAttendeeIdAndEventId_returnsMatchingReview() {
        Review a = repo.save(new Review(200L, 20L, "Mine", 2));
        repo.save(new Review(200L, 21L, "Not mine", 3));

        Optional<Review> opt = repo.findByAttendeeIdAndEventId(20L, 200L);

        assertTrue(opt.isPresent(), "Should find the review by attendee 20 on event 200");
        assertEquals(a.getId(), opt.get().getId());
        assertEquals("Mine", opt.get().getReviewText());
    }
}
