package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import id.ac.ui.cs.advprog.eventspherre.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/ads")
public class AdController {

    private final AdService adService;
    private final ImageStorageService imageStorageService;

    @Autowired
    public AdController(AdService adService,
                        ImageStorageService imageStorageService) {
        this.adService = adService;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String createAds(Model model) {
        return "ads/create-ads";
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String listAds(Model model) {
        model.addAttribute("ads", adService.getAllAds());
        return "ads/listads";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public String editAds (@PathVariable("id") Long id, Model model){
        model.addAttribute("id", id);
        model.addAttribute("ad", adService.getAd(id));
        return "ads/EditAds";
    }
}
