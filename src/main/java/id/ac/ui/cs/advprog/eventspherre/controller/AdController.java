package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.dto.AdRequestDto;
import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import id.ac.ui.cs.advprog.eventspherre.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;


import java.util.List;
import java.util.stream.Collectors;

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
    public String createAds(Model model) {
        return "create-ads";
    }

    @GetMapping("/edit/{id}")
    public String editAds(@PathVariable("id") Long id, Model model) {
        model.addAttribute("id", id);
        return "EditAds";
    }
}
