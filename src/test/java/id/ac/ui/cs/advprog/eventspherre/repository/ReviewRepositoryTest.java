package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReviewRepositoryTest {

    @Test
    void save_assignsId_andRetrievable() {
        ReviewRepository repo = new InMemoryReviewRepository();
        Review r = new Review(10L, 20L, "Hi", 4);

        assertNull(r.getId(), "ID should be null before save");

        Review saved = repo.save(r);
        assertNotNull(saved.getId(), "ID should be assigned after save");

        Optional<Review> found = repo.findById(saved.getId());
        assertTrue(found.isPresent(), "Saved review must be retrievable");
        assertEquals("Hi", found.get().getReviewText());
    }
}
