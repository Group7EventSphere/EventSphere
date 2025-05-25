package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.dto.AdRequestDto;
import id.ac.ui.cs.advprog.eventspherre.dto.AdResponseDTO;
import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.repository.AdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdServiceTest {

    @Mock private AdRepository adRepository;
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestBodyUriSpec uriSpec;
    @Mock private WebClient.RequestBodySpec bodySpec;
    @Mock private WebClient.RequestHeadersSpec<?> headersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    private AdService adService;
    private Ad adEntity;
    private AdRequestDto requestDto;

    @BeforeEach
    void setUp() {
        // --- test data ---
        adEntity = Ad.builder()
                .id(1L)
                .title("T")
                .description("D")
                .imageUrl("U")
                .build();
        requestDto = new AdRequestDto("T", "D", "U");

        // --- mock the remote DTO (package‐private ctor) ---
        AdResponseDTO responseDto = mock(AdResponseDTO.class);
        when(responseDto.getId()).thenReturn(1L);
        when(responseDto.getTitle()).thenReturn("T");
        when(responseDto.getDescription()).thenReturn("D");
        when(responseDto.getImageUrl()).thenReturn("U");

        // --- stub WebClient chain once for both REST tests ---
        when(webClientBuilder.baseUrl("http://localhost:8080/api"))
                .thenReturn(webClientBuilder);
        when(webClientBuilder.build())
                .thenReturn(webClient);

        doReturn(uriSpec).when(webClient).post();
        doReturn(bodySpec).when(uriSpec).uri("/api/ads");
        doReturn(headersSpec).when(bodySpec).bodyValue(requestDto);
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(Mono.just(responseDto))
                .when(responseSpec).bodyToMono(eq(AdResponseDTO.class));

        // --- finally construct the service under test ---
        adService = new AdService(webClientBuilder, adRepository);
    }

    @Test
    void testAddAdFromRest_success() {
        AdResponseDTO result = adService.addAdFromRest(requestDto);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("T", result.getTitle());

        verify(webClientBuilder).baseUrl("http://localhost:8080/api");
        verify(webClientBuilder).build();
        verify(webClient).post();
        verify(uriSpec).uri("/api/ads");
        verify(bodySpec).bodyValue(requestDto);
        verify(headersSpec).retrieve();
        verify(responseSpec).bodyToMono(AdResponseDTO.class);
    }

    @Test
    void testAddAdFromRest_noResponse_throws() {
        // override just the bodyToMono → empty()
        doReturn(Mono.empty())
                .when(responseSpec).bodyToMono(eq(AdResponseDTO.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                adService.addAdFromRest(requestDto)
        );
        assertEquals("No response from /api/ads", ex.getMessage());
    }

    @Test
    void testCreateAd() {
        when(adRepository.save(any(Ad.class))).thenReturn(adEntity);
        Ad created = adService.createAd(adEntity);
        assertSame(adEntity, created);
        verify(adRepository).save(adEntity);
    }

    @Test
    void testGetAdFound() {
        when(adRepository.findById(1L)).thenReturn(Optional.of(adEntity));
        Ad found = adService.getAd(1L);
        assertNotNull(found);
        assertEquals("T", found.getTitle());
        verify(adRepository).findById(1L);
    }

    @Test
    void testGetAdNotFound() {
        when(adRepository.findById(1L)).thenReturn(Optional.empty());
        assertNull(adService.getAd(1L));
        verify(adRepository).findById(1L);
    }

    @Test
    void testGetAllAds() {
        Ad other = Ad.builder()
                .id(2L).title("X").description("Y").imageUrl("Z").build();
        when(adRepository.findAll()).thenReturn(Arrays.asList(adEntity, other));

        List<Ad> list = adService.getAllAds();
        assertEquals(2, list.size());
        assertEquals("T", list.get(0).getTitle());
        assertEquals("X", list.get(1).getTitle());
        verify(adRepository).findAll();
    }

    @Test
    void testUpdateAdFound() {
        Ad updated = Ad.builder()
                .id(1L).title("TT").description("DD").imageUrl("UU").build();
        when(adRepository.existsById(1L)).thenReturn(true);
        when(adRepository.save(any(Ad.class))).thenReturn(updated);

        Ad out = adService.updateAd(1L, updated);
        assertNotNull(out);
        assertEquals("TT", out.getTitle());
        verify(adRepository).existsById(1L);
        verify(adRepository).save(argThat(a -> a.getId().equals(1L)));
    }

    @Test
    void testUpdateAdNotFound() {
        when(adRepository.existsById(1L)).thenReturn(false);
        assertNull(adService.updateAd(1L, adEntity));
        verify(adRepository).existsById(1L);
        verify(adRepository, never()).save(any());
    }

    @Test
    void testDeleteAd() {
        doNothing().when(adRepository).deleteById(1L);

        adService.deleteAd(1L);
        verify(adRepository).deleteById(1L);
    }
}
