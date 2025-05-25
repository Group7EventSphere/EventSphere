package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Event;
import id.ac.ui.cs.advprog.eventspherre.model.PromoCode;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.repository.PromoCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {
    
    @Mock
    private PromoCodeRepository promoCodeRepository;
    
    @InjectMocks
    private PromoCodeServiceImpl promoCodeService;
    
    private User organizer;
    private PromoCode promoCode;
    private Integer promoCodeId;
    
    @BeforeEach
    void setUp() {
        promoCodeId = 1;
        
        organizer = new User();
        organizer.setId(1);
        organizer.setName("Organizer One");
        organizer.setEmail("organizer1@test.com");
        
        promoCode = PromoCode.builder()
                .id(promoCodeId)
                .code("SAVE20")
                .description("Save 20% on your ticket")
                .discountPercentage(new BigDecimal("20.00"))
                .validFrom(LocalDate.now().minusDays(1))
                .validUntil(LocalDate.now().plusDays(7))
                .maxUsage(100)
                .currentUsage(0)
                .isActive(true)
                .organizerId(organizer.getId())
                .build();
    }
    
    @Test
    void testCreatePromoCode() {
        when(promoCodeRepository.existsByCode("SAVE20")).thenReturn(false);
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCode);
        
        PromoCode created = promoCodeService.createPromoCode(promoCode, organizer);
        
        assertNotNull(created);
        assertEquals("SAVE20", created.getCode());
        assertEquals(organizer.getId(), created.getOrganizerId());
        assertEquals(0, created.getCurrentUsage());
        assertTrue(created.getIsActive());
        
        verify(promoCodeRepository, times(1)).existsByCode("SAVE20");
        verify(promoCodeRepository, times(1)).save(any(PromoCode.class));
    }
    
    @Test
    void testCreatePromoCodeWithExistingCode() {
        when(promoCodeRepository.existsByCode("SAVE20")).thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, () -> {
            promoCodeService.createPromoCode(promoCode, organizer);
        });
        
        verify(promoCodeRepository, times(1)).existsByCode("SAVE20");
        verify(promoCodeRepository, never()).save(any(PromoCode.class));
    }
    
    @Test
    void testGetPromoCodeById() {
        when(promoCodeRepository.findById(promoCodeId)).thenReturn(Optional.of(promoCode));
        
        PromoCode found = promoCodeService.getPromoCodeById(promoCodeId);
        
        assertNotNull(found);
        assertEquals(promoCodeId, found.getId());
        assertEquals("SAVE20", found.getCode());
        
        verify(promoCodeRepository, times(1)).findById(promoCodeId);
    }
    
    @Test
    void testGetPromoCodeByIdNotFound() {
        when(promoCodeRepository.findById(promoCodeId)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            promoCodeService.getPromoCodeById(promoCodeId);
        });
        
        verify(promoCodeRepository, times(1)).findById(promoCodeId);
    }
    
    @Test
    void testGetPromoCodeByCode() {
        when(promoCodeRepository.findByCode("SAVE20")).thenReturn(Optional.of(promoCode));
        
        PromoCode found = promoCodeService.getPromoCodeByCode("SAVE20");
        
        assertNotNull(found);
        assertEquals("SAVE20", found.getCode());
        
        verify(promoCodeRepository, times(1)).findByCode("SAVE20");
    }
    
    @Test
    void testGetAllPromoCodesByOrganizer() {
        List<PromoCode> promoCodes = Arrays.asList(promoCode);
        when(promoCodeRepository.findByOrganizerId(organizer.getId())).thenReturn(promoCodes);
        
        List<PromoCode> found = promoCodeService.getAllPromoCodesByOrganizer(organizer);
        
        assertEquals(1, found.size());
        assertEquals(promoCode, found.get(0));
        
        verify(promoCodeRepository, times(1)).findByOrganizerId(organizer.getId());
    }
    
    @Test
    void testGetActivePromoCodesByOrganizer() {
        List<PromoCode> promoCodes = Arrays.asList(promoCode);
        when(promoCodeRepository.findByOrganizerIdAndIsActive(organizer.getId(), true)).thenReturn(promoCodes);
        
        List<PromoCode> found = promoCodeService.getActivePromoCodesByOrganizer(organizer);
        
        assertEquals(1, found.size());
        assertEquals(promoCode, found.get(0));
        
        verify(promoCodeRepository, times(1)).findByOrganizerIdAndIsActive(organizer.getId(), true);
    }
    
    @Test
    void testSearchPromoCodesByOrganizerWithKeyword() {
        List<PromoCode> promoCodes = Arrays.asList(promoCode);
        when(promoCodeRepository.searchByOrganizerIdAndKeyword(organizer.getId(), "save")).thenReturn(promoCodes);
        
        List<PromoCode> found = promoCodeService.searchPromoCodesByOrganizer(organizer, "save");
        
        assertEquals(1, found.size());
        assertEquals(promoCode, found.get(0));
        
        verify(promoCodeRepository, times(1)).searchByOrganizerIdAndKeyword(organizer.getId(), "save");
    }
    
    @Test
    void testSearchPromoCodesByOrganizerWithEmptyKeyword() {
        List<PromoCode> promoCodes = Arrays.asList(promoCode);
        when(promoCodeRepository.findByOrganizerId(organizer.getId())).thenReturn(promoCodes);
        
        List<PromoCode> found = promoCodeService.searchPromoCodesByOrganizer(organizer, "");
        
        assertEquals(1, found.size());
        assertEquals(promoCode, found.get(0));
        
        verify(promoCodeRepository, times(1)).findByOrganizerId(organizer.getId());
    }
    
    @Test
    void testUpdatePromoCode() {
        PromoCode updatedPromoCode = PromoCode.builder()
                .code("SAVE30")
                .description("Save 30% on your ticket")
                .discountPercentage(new BigDecimal("30.00"))
                .validFrom(LocalDate.now().minusDays(2))
                .validUntil(LocalDate.now().plusDays(10))
                .maxUsage(150)
                .isActive(true)
                .build();
        
        when(promoCodeRepository.findById(promoCodeId)).thenReturn(Optional.of(promoCode));
        when(promoCodeRepository.existsByCode("SAVE30")).thenReturn(false);
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCode);
        
        PromoCode updated = promoCodeService.updatePromoCode(promoCodeId, updatedPromoCode, organizer);
        
        assertNotNull(updated);
        verify(promoCodeRepository, times(1)).save(any(PromoCode.class));
    }
    
    @Test
    void testUpdatePromoCodeUnauthorized() {
        User anotherOrganizer = new User();
        anotherOrganizer.setId(2);
        
        when(promoCodeRepository.findById(promoCodeId)).thenReturn(Optional.of(promoCode));
        
        assertThrows(IllegalArgumentException.class, () -> {
            promoCodeService.updatePromoCode(promoCodeId, promoCode, anotherOrganizer);
        });
        
        verify(promoCodeRepository, never()).save(any(PromoCode.class));
    }
    
    @Test
    void testDeletePromoCode() {
        when(promoCodeRepository.findById(promoCodeId)).thenReturn(Optional.of(promoCode));
        
        promoCodeService.deletePromoCode(promoCodeId, organizer);
        
        verify(promoCodeRepository, times(1)).delete(promoCode);
    }
    
    @Test
    void testDeletePromoCodeUnauthorized() {
        User anotherOrganizer = new User();
        anotherOrganizer.setId(2);
        
        when(promoCodeRepository.findById(promoCodeId)).thenReturn(Optional.of(promoCode));
        
        assertThrows(IllegalArgumentException.class, () -> {
            promoCodeService.deletePromoCode(promoCodeId, anotherOrganizer);
        });
        
        verify(promoCodeRepository, never()).delete(any(PromoCode.class));
    }
    
    @Test
    void testIsPromoCodeValid() {
        when(promoCodeRepository.findByCode("SAVE20")).thenReturn(Optional.of(promoCode));
        
        boolean isValid = promoCodeService.isPromoCodeValid("SAVE20");
        
        assertTrue(isValid);
        verify(promoCodeRepository, times(1)).findByCode("SAVE20");
    }
    
    @Test
    void testIsPromoCodeValidNotFound() {
        when(promoCodeRepository.findByCode("NOTEXIST")).thenReturn(Optional.empty());
        
        boolean isValid = promoCodeService.isPromoCodeValid("NOTEXIST");
        
        assertFalse(isValid);
        verify(promoCodeRepository, times(1)).findByCode("NOTEXIST");
    }
    
    @Test
    void testDeactivateExpiredPromoCodes() {
        List<PromoCode> expiredCodes = Arrays.asList(promoCode);
        when(promoCodeRepository.findExpiredPromoCodes(any(LocalDate.class))).thenReturn(expiredCodes);
        when(promoCodeRepository.save(any(PromoCode.class))).thenReturn(promoCode);
        
        promoCodeService.deactivateExpiredPromoCodes();
        
        verify(promoCodeRepository, times(1)).findExpiredPromoCodes(any(LocalDate.class));
        verify(promoCodeRepository, times(1)).save(promoCode);
        assertFalse(promoCode.getIsActive());
    }
    
    @Test
    void testIsCodeAvailable() {
        when(promoCodeRepository.existsByCode("NEWCODE")).thenReturn(false);
        when(promoCodeRepository.existsByCode("SAVE20")).thenReturn(true);
        
        assertTrue(promoCodeService.isCodeAvailable("NEWCODE"));
        assertFalse(promoCodeService.isCodeAvailable("SAVE20"));
        
        verify(promoCodeRepository, times(1)).existsByCode("NEWCODE");
        verify(promoCodeRepository, times(1)).existsByCode("SAVE20");
    }
}