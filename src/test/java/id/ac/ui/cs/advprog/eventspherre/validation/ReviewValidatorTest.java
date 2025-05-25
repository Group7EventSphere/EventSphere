package id.ac.ui.cs.advprog.eventspherre.validation;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReviewValidatorTest {

    @Test
    void testValidReview() {
        Review review = new Review(1, 101L, "Excellent event!", 5);
        DefaultReviewValidator validator = new DefaultReviewValidator();
        assertTrue(validator.isValid(review));
    }

    @Test
    void testInvalidReviewEmptyText() {
        Review review = new Review(1, 101L, "", 5);
        DefaultReviewValidator validator = new DefaultReviewValidator();
        assertFalse(validator.isValid(review));
        assertEquals(
                "Invalid review: Review text cannot be empty and rating must be between 1 and 5.",
                validator.getErrorMessage()
        );
    }

    @Test
    void testInvalidReviewRatingOutOfRange() {
        Review review = new Review(1, 101L, "Not bad", 8);
        DefaultReviewValidator validator = new DefaultReviewValidator();
        assertFalse(validator.isValid(review));
    }
}
