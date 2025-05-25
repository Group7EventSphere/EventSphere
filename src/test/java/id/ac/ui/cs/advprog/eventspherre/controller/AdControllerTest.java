package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.dto.AdRequestDto;
import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import id.ac.ui.cs.advprog.eventspherre.service.ImageStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdRestControllerTest {

    private MockMvc mockMvc;

    @Mock private AdService adService;
    @Mock private ImageStorageService imageStorageService;

    @InjectMocks
    private AdRestController restController;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(restController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    private AdRequestDto dto() {
        return AdRequestDto.builder()
                .title("Test Ad")
                .description("Description")
                .imageUrl("image.jpg")
                .build();
    }

    private Ad entity(Long id) {
        return Ad.builder()
                .id(id)
                .title("Test Ad")
                .description("Description")
                .imageUrl("image.jpg")
                .build();
    }

    @Test
    void testCreateAd() throws Exception {
        given(adService.createAd(any(Ad.class))).willReturn(entity(1L));

        mockMvc.perform(post("/api/ads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Ad"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.imageUrl").value("image.jpg"));
    }

    @Test
    void testGetAd() throws Exception {
        given(adService.getAd(1L)).willReturn(entity(1L));

        mockMvc.perform(get("/api/ads/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Ad"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.imageUrl").value("image.jpg"));
    }

    @Test
    void testGetAdNotFound() throws Exception {
        given(adService.getAd(1L)).willReturn(null);

        mockMvc.perform(get("/api/ads/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testGetAllAds() throws Exception {
        given(adService.getAllAds())
                .willReturn(Collections.singletonList(entity(2L)));

        mockMvc.perform(get("/api/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Ad"))
                .andExpect(jsonPath("$[0].description").value("Description"))
                .andExpect(jsonPath("$[0].imageUrl").value("image.jpg"));
    }

    @Test
    void testUpdateAd() throws Exception {
        given(adService.updateAd(eq(5L), any(Ad.class)))
                .willReturn(entity(5L));

        mockMvc.perform(put("/api/ads/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Ad"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.imageUrl").value("image.jpg"));
    }

    @Test
    void testUpdateAdNotFound() throws Exception {
        given(adService.updateAd(eq(9L), any(Ad.class))).willReturn(null);

        mockMvc.perform(put("/api/ads/{id}", 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto())))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testDeleteAd() throws Exception {
        doNothing().when(adService).deleteAd(3L);

        mockMvc.perform(delete("/api/ads/{id}", 3L))
                .andExpect(status().isNoContent());
    }
}
