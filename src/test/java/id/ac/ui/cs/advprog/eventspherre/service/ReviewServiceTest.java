package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.repository.InMemoryReviewRepository;
import id.ac.ui.cs.advprog.eventspherre.validation.DefaultReviewValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.NoSuchElementException;

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

    @Test
    void update_existingReview_updatesFields() {
        Review original = service.create(new Review(10L,20L,"Old",2));
        Review update = new Review(10L,20L,"New text",5);

        Review result = service.update(original.getId(), update);

        assertEquals("New text", result.getReviewText());
        assertEquals(5, result.getRating());
    }

    @Test
    void update_nonExisting_throwsNoSuchElement() {
        Review update = new Review(1L,1L,"X",1);
        assertThrows(NoSuchElementException.class,
                () -> service.update(999L, update),
                "Should throw when updating missing review");
    }

    @Test
    void delete_existing_returnsTrueAndRemoves() {
        Review r = service.create(new Review(5L,5L,"T",1));
        boolean ok = service.delete(r.getId());

        assertTrue(ok);
        assertFalse(service.findById(r.getId()).isPresent());
    }

    @Test
    void delete_nonExisting_returnsFalse() {
        assertFalse(service.delete(123L), "Deleting missing should return false");
    }
}
