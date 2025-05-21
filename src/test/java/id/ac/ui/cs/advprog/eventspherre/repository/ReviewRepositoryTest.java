package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
}
