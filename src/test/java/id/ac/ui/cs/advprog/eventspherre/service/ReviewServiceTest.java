package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.repository.InMemoryReviewRepository;
import id.ac.ui.cs.advprog.eventspherre.validation.DefaultReviewValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReviewServiceTest {

    private ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewServiceImpl(
                new InMemoryReviewRepository(),
                new DefaultReviewValidator()
        );
    }

    @Test
    void create_validReview_assignsId() {
        Review input = new Review(10L, 20L, "Great!", 5);
        Review saved = service.create(input);

        assertNotNull(saved.getId(), "ID must be assigned");
        assertEquals("Great!", saved.getReviewText());
    }

    @Test
    void create_invalidReview_throwsException() {
        Review bad = new Review(10L, 20L, "", 8);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(bad)
        );
        assertTrue(ex.getMessage().contains("Invalid review"));
    }

    @Test
    void findById_existingId_returnsReview() {
        Review created = service.create(new Review(11L, 21L, "Test", 4));
        Optional<Review> found = service.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(4, found.get().getRating());
    }
}
