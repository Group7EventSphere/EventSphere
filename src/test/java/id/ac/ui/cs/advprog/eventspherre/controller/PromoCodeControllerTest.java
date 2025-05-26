package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.config.WebSecurityTestConfig;
import id.ac.ui.cs.advprog.eventspherre.model.PromoCode;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.PromoCodeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@WebMvcTest(PromoCodeController.class)
@Import(WebSecurityTestConfig.class)
@ActiveProfiles("test")
class PromoCodeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PromoCodeService promoCodeService;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private AuthenticationProvider authenticationProvider;
    
    private User organizer;
    private PromoCode promoCode;
    
    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1);
        organizer.setName("Test Organizer");
        organizer.setEmail("organizer@test.com");
        organizer.setRole(User.Role.ORGANIZER);
        
        promoCode = PromoCode.builder()
                .id(1)
                .code("TEST20")
                .description("Test discount")
                .discountPercentage(new BigDecimal("20.00"))
                .validFrom(LocalDate.now())
                .validUntil(LocalDate.now().plusDays(30))
                .maxUsage(100)
                .currentUsage(0)
                .isActive(true)
                .organizerId(1)
                .build();
    }
    
    @Test
    @WithMockUser(username = "organizer@test.com", roles = {"ORGANIZER"})
    void testListPromoCodes() throws Exception {
        when(userService.findByEmail("organizer@test.com")).thenReturn(organizer);
        when(promoCodeService.getAllPromoCodesByOrganizer(organizer))
                .thenReturn(Arrays.asList(promoCode));
        
        mockMvc.perform(get("/promo-codes")
                        .with(user("organizer@test.com").roles("ORGANIZER")))
                .andExpect(status().isOk())
                .andExpect(view().name("promo-code/list"))
                .andExpect(model().attributeExists("promoCodes"));
    }
    
    @Test
    @WithMockUser(username = "organizer@test.com", roles = {"ORGANIZER"})
    void testSearchPromoCodes() throws Exception {
        when(userService.findByEmail("organizer@test.com")).thenReturn(organizer);
        when(promoCodeService.searchPromoCodesByOrganizer(organizer, "TEST"))
                .thenReturn(Arrays.asList(promoCode));
        
        mockMvc.perform(get("/promo-codes")
                        .param("search", "TEST")
                        .with(user("organizer@test.com").roles("ORGANIZER")))
                .andExpect(status().isOk())
                .andExpect(view().name("promo-code/list"))
                .andExpect(model().attributeExists("promoCodes", "search"));
    }
    
    @Test
    @WithMockUser(username = "organizer@test.com", roles = {"ORGANIZER"})
    void testShowCreateForm() throws Exception {
        when(userService.findByEmail("organizer@test.com")).thenReturn(organizer);
        
        mockMvc.perform(get("/promo-codes/create")
                        .with(user("organizer@test.com").roles("ORGANIZER")))
                .andExpect(status().isOk())
                .andExpect(view().name("promo-code/form"))
                .andExpect(model().attributeExists("promoCode"));
    }
    
    @Test
    @WithMockUser(username = "organizer@test.com", roles = {"ORGANIZER"})
    void testCreatePromoCode() throws Exception {
        when(userService.findByEmail("organizer@test.com")).thenReturn(organizer);
        when(promoCodeService.createPromoCode(any(PromoCode.class), eq(organizer)))
                .thenReturn(promoCode);
        
        mockMvc.perform(post("/promo-codes/create")
                        .param("code", "TEST20")
                        .param("description", "Test discount")
                        .param("discountPercentage", "20.00")
                        .param("validFrom", "2024-01-01")
                        .param("validUntil", "2024-12-31")
                        .param("maxUsage", "100")
                        .with(user("organizer@test.com").roles("ORGANIZER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/promo-codes"))
                .andExpect(flash().attribute("successMessage", "Promo code created successfully!"));
    }
    
    @Test
    @WithMockUser(username = "organizer@test.com", roles = {"ORGANIZER"})
    void testDeletePromoCode() throws Exception {
        when(userService.findByEmail("organizer@test.com")).thenReturn(organizer);
        doNothing().when(promoCodeService).deletePromoCode(1, organizer);
        
        mockMvc.perform(post("/promo-codes/delete/1")
                        .with(user("organizer@test.com").roles("ORGANIZER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/promo-codes"))
                .andExpect(flash().attribute("successMessage", "Promo code deleted successfully!"));
    }
    
    @Test
    @WithMockUser(username = "organizer@test.com", roles = {"ORGANIZER"})
    void testCheckCodeAvailability() throws Exception {
        when(promoCodeService.isCodeAvailable("NEWCODE")).thenReturn(true);
        
        mockMvc.perform(get("/promo-codes/check-availability")
                        .param("code", "NEWCODE")
                        .with(user("organizer@test.com").roles("ORGANIZER")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
    
    @Test
    @WithMockUser(username = "organizer@test.com", roles = {"ORGANIZER"})
    void testShowEditForm() throws Exception {
        when(userService.findByEmail("organizer@test.com")).thenReturn(organizer);
        when(promoCodeService.getPromoCodeById(1)).thenReturn(promoCode);
        
        mockMvc.perform(get("/promo-codes/edit/1")
                        .with(user("organizer@test.com").roles("ORGANIZER")))
                .andExpect(status().isOk())
                .andExpect(view().name("promo-code/form"))
                .andExpect(model().attributeExists("promoCode"));
    }
    
    @Test
    @WithMockUser(username = "organizer@test.com", roles = {"ORGANIZER"})
    void testUpdatePromoCode() throws Exception {
        when(userService.findByEmail("organizer@test.com")).thenReturn(organizer);
        when(promoCodeService.updatePromoCode(eq(1), any(PromoCode.class), eq(organizer)))
                .thenReturn(promoCode);
        
        mockMvc.perform(post("/promo-codes/edit/1")
                        .param("code", "TEST30")
                        .param("description", "Updated discount")
                        .param("discountPercentage", "30.00")
                        .param("validFrom", "2024-01-01")
                        .param("validUntil", "2024-12-31")
                        .param("maxUsage", "150")
                        .param("isActive", "true")
                        .with(user("organizer@test.com").roles("ORGANIZER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/promo-codes"))
                .andExpect(flash().attribute("successMessage", "Promo code updated successfully!"));
    }
}