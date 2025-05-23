package id.ac.ui.cs.advprog.eventspherre.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventspherre.model.Review;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReviewControllerTest {

    private MockMvc mvc;
    private ReviewService service;
    private ObjectMapper mapper = new ObjectMapper();
    private User fakeUser;

    @BeforeEach
    void setUp() {
        service = mock(ReviewService.class);

        fakeUser = new User();
        fakeUser.setId(2);

        HandlerMethodArgumentResolver authPrincipalResolver =
                new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(User.class)
                                && parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter,
                                                  ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest,
                                                  WebDataBinderFactory binderFactory) {
                        return fakeUser;
                    }
                };

        mvc = MockMvcBuilders
                .standaloneSetup(new ReviewController(service))
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    void createReview_returnsCreated() throws Exception {
        Review in  = new Review(1, 0L, "Nice!", 4);
        Review out = new Review(1, 2L, "Nice!", 4);
        out.setId(5L);

        when(service.create(any(Review.class))).thenReturn(out);

        mvc.perform(post("/reviews/create/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reviews/5"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getReview_returnsOk() throws Exception {
        Review r = new Review(3, 0L, "Ok", 3);
        r.setId(7L);
        when(service.findById(7L)).thenReturn(Optional.of(r));

        mvc.perform(get("/reviews/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3));
    }

    @Test
    void getByEvent_returnsList() throws Exception {
        Review a = new Review(7, 0L, "Good", 5); a.setId(1L);
        Review b = new Review(7, 0L, "Bad", 1);  b.setId(2L);
        when(service.findByEventId(7)).thenReturn(List.of(a, b));

        mvc.perform(get("/reviews/event/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].reviewText").value("Bad"));
    }

    @Test
    void updateReview_returnsOk() throws Exception {
        Review in  = new Review(1, 0L, "X", 4);
        Review out = new Review(1, 2L, "X", 4);
        out.setId(1L);
        when(service.update(eq(1L), any(Review.class))).thenReturn(out);

        mvc.perform(put("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewText").value("X"))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void updateMissing_returnsNotFound() throws Exception {
        Review in = new Review(1, 0L, "Y", 5);
        when(service.update(eq(2L), any(Review.class)))
                .thenThrow(new NoSuchElementException("Review not found"));

        mvc.perform(put("/reviews/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_returnsNoContent() throws Exception {
        when(service.delete(1L)).thenReturn(true);

        mvc.perform(delete("/reviews/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMissing_returnsNotFound() throws Exception {
        when(service.delete(2L)).thenReturn(false);

        mvc.perform(delete("/reviews/2"))
                .andExpect(status().isNotFound());
    }
}

