package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
// now answers both /reviews/** and /api/reviews/**
@RequestMapping({ "/reviews", "/api/reviews" })
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping("/create/{eventId}")
    @PreAuthorize("hasAnyRole('ATTENDEE')")
    public ResponseEntity<Review> create(
            @PathVariable int eventId,
            @RequestBody Review review,
            @AuthenticationPrincipal User user
    ) {
        review.setEventId(eventId);
        review.setAttendeeId(user.getId().longValue());
        Review saved = service.create(review);

        return ResponseEntity
                .created(URI.create("/reviews/" + saved.getId()))
                .body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable Long id) {
        Optional<Review> found = service.findById(id);
        return found.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Review>> getByEventId(@PathVariable int eventId) {
        return ResponseEntity.ok(service.findByEventId(eventId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATTENDEE')")
    public ResponseEntity<Review> update(
            @PathVariable Long id,
            @RequestBody Review review,
            @AuthenticationPrincipal User user
    ) {
        try {
            review.setAttendeeId(user.getId().longValue());
            Review updated = service.update(id, review);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATTENDEE')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        boolean deleted = service.delete(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
