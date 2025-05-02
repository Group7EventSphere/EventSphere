package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdService adService;

    @InjectMocks
    private AdController adController;

    private Ad ad;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adController).build();
        ad = new Ad(1L, "Test Ad", "Description", "image.jpg", "ADMIN", true);
    }

    @Test
    void testCreateAd() throws Exception {
        when(adService.createAd(any(Ad.class))).thenReturn(ad);

        mockMvc.perform(post("/api/ads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test Ad\",\"description\":\"Description\",\"imageUrl\":\"image.jpg\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Ad"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    void testGetAd() throws Exception {
        when(adService.getAd(anyLong())).thenReturn(ad);

        mockMvc.perform(get("/api/ads/{id}", ad.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Ad"))
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    void testGetAllAds() throws Exception {
        when(adService.getAllAds()).thenReturn(java.util.Collections.singletonList(ad));

        mockMvc.perform(get("/api/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Ad"))
                .andExpect(jsonPath("$[0].description").value("Description"));
    }

    @Test
    void testUpdateAd() throws Exception {
        when(adService.updateAd(anyLong(), any(Ad.class))).thenReturn(ad);

        mockMvc.perform(put("/api/ads/{id}", ad.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Test Ad\",\"description\":\"Updated Description\",\"imageUrl\":\"updated_image.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Test Ad"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    void testDeleteAd() throws Exception {
        doNothing().when(adService).deleteAd(anyLong());

        mockMvc.perform(delete("/api/ads/{id}", ad.getId()))
                .andExpect(status().isNoContent());

        verify(adService, times(1)).deleteAd(ad.getId());  // Ensure deleteAd was called once
    }
}
