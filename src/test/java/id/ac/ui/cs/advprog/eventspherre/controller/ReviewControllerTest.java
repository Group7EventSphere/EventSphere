package id.ac.ui.cs.advprog.eventspherre.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReviewControllerTest {

    private MockMvc mvc;
    private ReviewService service;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = mock(ReviewService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ReviewController(service)).build();
    }

    @Test
    void postValidReview_returns201() throws Exception {
        Review in = new Review(1L,2L,"Nice!",4);
        Review out = new Review(1L,2L,"Nice!",4);
        out.setId(5L);

        when(service.create(any())).thenReturn(out);

        mvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getExistingReview_returns200() throws Exception {
        Review r = new Review(3L,4L,"Ok",3);
        r.setId(7L);
        when(service.findById(7L)).thenReturn(Optional.of(r));

        mvc.perform(get("/reviews/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3));
    }
}
