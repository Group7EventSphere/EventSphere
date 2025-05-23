package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.repository.ReviewRepository;
import id.ac.ui.cs.advprog.eventspherre.validation.DefaultReviewValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private ReviewRepository repo;
    private ReviewServiceImpl svc;

    @BeforeEach
    void init() {
        repo = mock(ReviewRepository.class);
        svc  = new ReviewServiceImpl(repo, new DefaultReviewValidator());
    }

    @Test
    void createAndSave() {
        Review toSave = new Review(1, 2L, "Nice!", 5);
        Review saved  = new Review(1, 2L, "Nice!", 5);
        saved.setId(42L);

        when(repo.save(toSave)).thenReturn(saved);

        Review result = svc.create(toSave);
        assertEquals(42L, result.getId());
        assertEquals("Nice!", result.getReviewText());
        verify(repo).save(toSave);
    }

    @Test
    void createDuplicate_throwsIllegalArgumentException() {
        Review dup = new Review(1, 2L, "Dup", 4);
        when(repo.findByAttendeeIdAndEventId(2L, 1))
                .thenReturn(Optional.of(new Review(1, 2L, "Old", 5)));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> svc.create(dup)
        );
        assertTrue(ex.getMessage().contains("already submitted"));
        verify(repo, never()).save(any());
    }

    @Test
    void createInvalidThrows() {
        Review bad = new Review(1, 2L, "", 8);
        assertThrows(IllegalArgumentException.class,
                () -> svc.create(bad));
        verify(repo, never()).save(any());
    }

    @Test
    void findById() {
        Review r = new Review(1, 2L, "OK", 4);
        r.setId(5L);
        when(repo.findById(5L)).thenReturn(Optional.of(r));

        Optional<Review> opt = svc.findById(5L);
        assertTrue(opt.isPresent());
        assertEquals(4, opt.get().getRating());
    }

    @Test
    void updateReview() {
        Review original = new Review(1, 2L, "Old", 2);
        original.setId(10L);
        Review update = new Review(1, 2L, "New", 4);

        when(repo.findById(10L)).thenReturn(Optional.of(original));
        when(repo.save(original)).thenReturn(original);

        Review out = svc.update(10L, update);
        assertEquals("New", out.getReviewText());
        assertEquals(4, out.getRating());
        verify(repo).save(original);
    }

    @Test
    void updateNonExistingThrows() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> svc.update(99L, new Review(0,0L,"X",1)));
    }

    @Test
    void updateInvalid_throwsIllegalArgument() {
        Review existing = new Review(1, 2L, "Okay", 3);
        existing.setId(11L);
        // invalid new data: rating out of bounds
        Review bad = new Review(1, 2L, "Bad", 10);

        when(repo.findById(11L)).thenReturn(Optional.of(existing));
        assertThrows(IllegalArgumentException.class,
                () -> svc.update(11L, bad));
        verify(repo, never()).save(any());
    }

    @Test
    void deleteExisting() {
        when(repo.existsById(7L)).thenReturn(true);
        assertTrue(svc.delete(7L));
        verify(repo).deleteById(7L);
    }

    @Test
    void deleteNonExisting() {
        when(repo.existsById(8L)).thenReturn(false);
        assertFalse(svc.delete(8L));
        verify(repo, never()).deleteById(any());
    }
}

