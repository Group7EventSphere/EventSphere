package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdService adService;

    @InjectMocks
    private AdController adController;

    private Ad ad;
    private final String BASE_URL = "/api/ads";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adController).build();
        ad = new Ad(1L, "Test Ad", "Description", "image.jpg", "ADMIN");
    }

    @Test
    void testCreateAd() throws Exception {
        when(adService.createAd(any(Ad.class))).thenReturn(ad);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"title\":\"Test Ad\",\"description\":\"Description\",\"imageUrl\":\"image.jpg\",\"creatorId\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Ad"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.imageUrl").value("image.jpg"));

        verify(adService, times(1)).createAd(any(Ad.class));
    }

    @Test
    void testGetAd() throws Exception {
        when(adService.getAd(anyLong())).thenReturn(ad);

        mockMvc.perform(get(BASE_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Ad"))
                .andExpect(jsonPath("$.description").value("Description"));

        verify(adService, times(1)).getAd(1L);
    }

    @Test
    void testGetAdNotFound() throws Exception {
        when(adService.getAd(anyLong())).thenReturn(null);

        mockMvc.perform(get(BASE_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(adService, times(1)).getAd(1L);
    }

    @Test
    void testGetAllAds() throws Exception {
        List<Ad> ads = Collections.singletonList(ad);
        when(adService.getAllAds()).thenReturn(ads);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Ad"))
                .andExpect(jsonPath("$[0].description").value("Description"));

        verify(adService, times(1)).getAllAds();
    }

    @Test
    void testUpdateAd() throws Exception {
        Ad updatedAd = new Ad(1L, "Updated Ad", "Updated Description", "updated.jpg", "ADMIN");
        when(adService.updateAd(anyLong(), any(Ad.class))).thenReturn(updatedAd);

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"title\":\"Updated Ad\",\"description\":\"Updated Description\",\"imageUrl\":\"updated.jpg\",\"creatorId\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Ad"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        verify(adService, times(1)).updateAd(eq(1L), any(Ad.class));
    }

    @Test
    void testUpdateAdNotFound() throws Exception {
        when(adService.updateAd(anyLong(), any(Ad.class))).thenReturn(null);

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"title\":\"Updated Ad\",\"description\":\"Updated Description\",\"imageUrl\":\"updated.jpg\",\"creatorId\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(adService, times(1)).updateAd(eq(1L), any(Ad.class));
    }

    @Test
    void testDeleteAd() throws Exception {
        doNothing().when(adService).deleteAd(anyLong());

        mockMvc.perform(delete(BASE_URL + "/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(adService, times(1)).deleteAd(1L);
    }
}