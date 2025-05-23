package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.repository.ReviewRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reviews")
public class ReviewUIController {

    private final ReviewRepository reviewRepository;

    public ReviewUIController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String reviewsPage(Model model) {
        model.addAttribute("reviews", reviewRepository.findAll());
        return "reviews";
    }
}
