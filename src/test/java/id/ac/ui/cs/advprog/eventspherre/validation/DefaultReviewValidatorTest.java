package id.ac.ui.cs.advprog.eventspherre.validation;

import id.ac.ui.cs.advprog.eventspherre.constants.AppConstants;
import id.ac.ui.cs.advprog.eventspherre.model.Review;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultReviewValidatorTest {

    private final DefaultReviewValidator validator = new DefaultReviewValidator();

    @Test
    void validReview_returnsTrue() {
        Review r = new Review(1, 1L, "Perfect!", 3);
        assertTrue(validator.isValid(r));
    }

    @Test
    void nullText_returnsFalse() {
        Review r = new Review(1, 1L, null, 3);
        assertFalse(validator.isValid(r));
    }

    @Test
    void blankText_returnsFalse() {
        Review r = new Review(1, 1L, "   ", 3);
        assertFalse(validator.isValid(r));
    }

    @Test
    void ratingTooLow_returnsFalse() {
        Review r = new Review(1, 1L, "Okay", AppConstants.MIN_RATING - 1);
        assertFalse(validator.isValid(r));
    }

    @Test
    void ratingTooHigh_returnsFalse() {
        Review r = new Review(1, 1L, "Okay", AppConstants.MAX_RATING + 1);
        assertFalse(validator.isValid(r));
    }

    @Test
    void getErrorMessage_returnsConstant() {
        assertEquals(AppConstants.ERROR_INVALID_REVIEW, validator.getErrorMessage());
    }
}
