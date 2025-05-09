package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Review> create(@RequestBody Review review) {
        Review saved = service.create(review);
        return ResponseEntity
                .created(URI.create("/reviews/" + saved.getId()))
                .body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable Long id) {
        Optional<Review> found = service.findById(id);
        return found
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> update(
            @PathVariable Long id,
            @RequestBody Review review
    ) {
        try {
            Review updated = service.update(id, review);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
