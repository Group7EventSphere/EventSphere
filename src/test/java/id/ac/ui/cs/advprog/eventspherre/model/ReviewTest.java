package id.ac.ui.cs.advprog.eventspherre.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    @Test
    void testGetReviewText() {
        Review review = new Review(1, 101L, "Excellent event!", 5);
        assertEquals("Excellent event!", review.getReviewText());
    }

    @Test
    void testGetRating() {
        Review review = new Review(1, 101L, "Mediocre event.", 3);
        assertEquals(3, review.getRating());
    }

    @Test
    void testGetIdBeforeAndAfterSet() {
        Review review = new Review(1, 101L, "Informative event", 4);
        assertNull(review.getId());
        review.setId(10L);
        assertNotNull(review.getId());
        assertEquals(10L, review.getId());
    }
}
