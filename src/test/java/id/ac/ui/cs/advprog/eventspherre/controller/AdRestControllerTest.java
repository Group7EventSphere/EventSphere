package id.ac.ui.cs.advprog.eventspherre.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventspherre.dto.AdRequestDto;
import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import id.ac.ui.cs.advprog.eventspherre.service.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdRestControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AdService adService;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private AdRestController adRestController;

    private AdRequestDto sampleDto;
    private Ad sampleAd;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adRestController).build();

        sampleDto = new AdRequestDto();
        sampleDto.setTitle("Sample Title");
        sampleDto.setDescription("Sample Description");
        sampleDto.setImageUrl("http://image.url");

        sampleAd = Ad.builder()
                .id(1L)
                .title("Sample Title")
                .description("Sample Description")
                .imageUrl("http://image.url")
                .build();
    }

    @Test
    void createAd_valid_shouldReturnCreatedDtoAndStatus201() throws Exception {
        when(adService.createAd(any(Ad.class))).thenReturn(sampleAd);

        mockMvc.perform(post("/api/ads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Sample Title"))
                .andExpect(jsonPath("$.description").value("Sample Description"))
                .andExpect(jsonPath("$.imageUrl").value("http://image.url"));
    }

    @Test
    void getAd_found_shouldReturnDtoAndStatus200() throws Exception {
        when(adService.getAd(1L)).thenReturn(sampleAd);

        mockMvc.perform(get("/api/ads/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample Title"))
                .andExpect(jsonPath("$.description").value("Sample Description"))
                .andExpect(jsonPath("$.imageUrl").value("http://image.url"));
    }

    @Test
    void getAd_notFound_shouldReturnOkWithNullBody() throws Exception {
        when(adService.getAd(1L)).thenReturn(null);

        mockMvc.perform(get("/api/ads/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void getAllAds_shouldReturnListOfAdsAndStatus200() throws Exception {
        when(adService.getAllAds()).thenReturn(List.of(sampleAd));

        mockMvc.perform(get("/api/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sample Title"));
    }

    @Test
    void updateAd_found_shouldReturnUpdatedDtoAndStatus200() throws Exception {
        when(adService.updateAd(eq(1L), any(Ad.class))).thenReturn(sampleAd);

        mockMvc.perform(put("/api/ads/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample Title"))
                .andExpect(jsonPath("$.description").value("Sample Description"))
                .andExpect(jsonPath("$.imageUrl").value("http://image.url"));
    }

    @Test
    void updateAd_notFound_shouldReturnOkWithNullBody() throws Exception {
        when(adService.updateAd(eq(1L), any(Ad.class))).thenReturn(null);

        mockMvc.perform(put("/api/ads/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void deleteAd_shouldReturnNoContentAndStatus204() throws Exception {
        doNothing().when(adService).deleteAd(1L);

        mockMvc.perform(delete("/api/ads/1"))
                .andExpect(status().isNoContent());
    }
}
