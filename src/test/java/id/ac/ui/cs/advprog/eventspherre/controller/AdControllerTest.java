package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import id.ac.ui.cs.advprog.eventspherre.service.ImageStorageService;
import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdControllerTest {

    @Mock
    private AdService adService;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private Model model;

    @InjectMocks
    private AdController adController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAds_shouldReturnCreateAdsView() {
        String viewName = adController.createAds(model);
        assertEquals("ads/create-ads", viewName);
    }

    @Test
    void testListAds_shouldReturnListAdsViewAndAddAttributes() {
        List<Ad> ads = List.of(new Ad(), new Ad());
        when(adService.getAllAds()).thenReturn(ads);

        String viewName = adController.listAds(model);

        verify(model).addAttribute("ads", ads);
        assertEquals("ads/listads", viewName);
    }

    @Test
    void testEditAds_shouldReturnEditAdsViewAndAddAttributes() {
        Long adId = 1L;
        Ad mockAd = new Ad();
        when(adService.getAd(adId)).thenReturn(mockAd);

        String viewName = adController.editAds(adId, model);

        verify(model).addAttribute("id", adId);
        verify(model).addAttribute("ad", mockAd);
        assertEquals("ads/EditAds", viewName);
    }
}