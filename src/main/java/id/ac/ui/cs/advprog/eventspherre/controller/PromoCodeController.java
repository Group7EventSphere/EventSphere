package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.PromoCode;
import id.ac.ui.cs.advprog.eventspherre.model.User;
import id.ac.ui.cs.advprog.eventspherre.service.PromoCodeService;
import id.ac.ui.cs.advprog.eventspherre.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {
    
    private final PromoCodeService promoCodeService;
    private final UserService userService;
    
    @GetMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public String listPromoCodes(@RequestParam(required = false) String search, 
                                 Model model, 
                                 Principal principal) {
        User organizer = userService.findByEmail(principal.getName());
        
        if (search != null && !search.isEmpty()) {
            model.addAttribute("promoCodes", promoCodeService.searchPromoCodesByOrganizer(organizer, search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("promoCodes", promoCodeService.getAllPromoCodesByOrganizer(organizer));
        }
        
        return "promo-code/list";
    }
    
    @GetMapping("/create")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String showCreateForm(Model model, Principal principal) {
        PromoCode promoCode = PromoCode.builder()
                .isActive(true)
                .currentUsage(0)
                .build();
        model.addAttribute("promoCode", promoCode);
        
        return "promo-code/form";
    }
    
    @PostMapping("/create")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String createPromoCode(@ModelAttribute PromoCode promoCode,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            User organizer = userService.findByEmail(principal.getName());
            promoCodeService.createPromoCode(promoCode, organizer);
            redirectAttributes.addFlashAttribute("successMessage", "Promo code created successfully!");
            return "redirect:/promo-codes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/promo-codes/create";
        }
    }
    
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String showEditForm(@PathVariable Integer id, Model model, Principal principal) {
        try {
            User organizer = userService.findByEmail(principal.getName());
            PromoCode promoCode = promoCodeService.getPromoCodeById(id);
            
            if (!promoCode.getOrganizerId().equals(organizer.getId())) {
                return "redirect:/promo-codes";
            }
            
            model.addAttribute("promoCode", promoCode);
            
            return "promo-code/form";
        } catch (Exception e) {
            return "redirect:/promo-codes";
        }
    }
    
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String updatePromoCode(@PathVariable Integer id,
                                  @ModelAttribute PromoCode promoCode,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            User organizer = userService.findByEmail(principal.getName());
            promoCodeService.updatePromoCode(id, promoCode, organizer);
            redirectAttributes.addFlashAttribute("successMessage", "Promo code updated successfully!");
            return "redirect:/promo-codes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/promo-codes/edit/" + id;
        }
    }
    
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String deletePromoCode(@PathVariable Integer id,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            User organizer = userService.findByEmail(principal.getName());
            promoCodeService.deletePromoCode(id, organizer);
            redirectAttributes.addFlashAttribute("successMessage", "Promo code deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/promo-codes";
    }
    
    @GetMapping("/check-availability")
    @PreAuthorize("hasRole('ORGANIZER')")
    @ResponseBody
    public boolean checkCodeAvailability(@RequestParam String code) {
        return promoCodeService.isCodeAvailable(code);
    }
}