package id.ac.ui.cs.advprog.eventspherre.repository;

import id.ac.ui.cs.advprog.eventspherre.model.PromoCode;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PromoCodeRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private PromoCodeRepository promoCodeRepository;
    
    private User organizer;
    private PromoCode promoCode1;
    private PromoCode promoCode2;
    
    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setName("Organizer One");
        organizer.setEmail("organizer1@test.com");
        organizer.setPassword("password");
        organizer.setRole(User.Role.ORGANIZER);
        organizer = entityManager.persistAndFlush(organizer);
        
        promoCode1 = PromoCode.builder()
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
        
        promoCode2 = PromoCode.builder()
                .code("SUMMER50")
                .description("Summer sale 50% off")
                .discountPercentage(new BigDecimal("50.00"))
                .validFrom(LocalDate.now().minusDays(10))
                .validUntil(LocalDate.now().minusDays(1)) // Expired
                .maxUsage(50)
                .currentUsage(0)
                .isActive(true)
                .organizerId(organizer.getId())
                .build();
        
        promoCode1 = entityManager.persistAndFlush(promoCode1);
        promoCode2 = entityManager.persistAndFlush(promoCode2);
    }
    
    @Test
    void testFindByCode() {
        Optional<PromoCode> found = promoCodeRepository.findByCode("SAVE20");
        
        assertTrue(found.isPresent());
        assertEquals("SAVE20", found.get().getCode());
        assertEquals("Save 20% on your ticket", found.get().getDescription());
    }
    
    @Test
    void testFindByCodeNotFound() {
        Optional<PromoCode> found = promoCodeRepository.findByCode("NOTEXIST");
        
        assertFalse(found.isPresent());
    }
    
    @Test
    void testFindByOrganizer() {
        List<PromoCode> promoCodes = promoCodeRepository.findByOrganizerId(organizer.getId());
        
        assertEquals(2, promoCodes.size());
        assertTrue(promoCodes.contains(promoCode1));
        assertTrue(promoCodes.contains(promoCode2));
    }
    
    @Test
    void testFindByOrganizerAndIsActive() {
        promoCode2.setIsActive(false);
        entityManager.persistAndFlush(promoCode2);
        
        List<PromoCode> activePromoCodes = promoCodeRepository.findByOrganizerIdAndIsActive(organizer.getId(), true);
        
        assertEquals(1, activePromoCodes.size());
        assertTrue(activePromoCodes.contains(promoCode1));
        assertFalse(activePromoCodes.contains(promoCode2));
    }
    
    @Test
    void testFindExpiredPromoCodes() {
        List<PromoCode> expiredPromoCodes = promoCodeRepository.findExpiredPromoCodes(LocalDate.now());
        
        assertEquals(1, expiredPromoCodes.size());
        assertTrue(expiredPromoCodes.contains(promoCode2));
        assertFalse(expiredPromoCodes.contains(promoCode1));
    }
    
    @Test
    void testSearchByOrganizerAndKeyword() {
        List<PromoCode> searchResults = promoCodeRepository.searchByOrganizerIdAndKeyword(organizer.getId(), "summer");
        
        assertEquals(1, searchResults.size());
        assertTrue(searchResults.contains(promoCode2));
        
        searchResults = promoCodeRepository.searchByOrganizerIdAndKeyword(organizer.getId(), "SAVE");
        
        assertEquals(1, searchResults.size());
        assertTrue(searchResults.contains(promoCode1));
        
        searchResults = promoCodeRepository.searchByOrganizerIdAndKeyword(organizer.getId(), "ticket");
        
        assertEquals(1, searchResults.size());
        assertTrue(searchResults.contains(promoCode1));
    }
    
    @Test
    void testExistsByCode() {
        assertTrue(promoCodeRepository.existsByCode("SAVE20"));
        assertTrue(promoCodeRepository.existsByCode("SUMMER50"));
        assertFalse(promoCodeRepository.existsByCode("NOTEXIST"));
    }
}