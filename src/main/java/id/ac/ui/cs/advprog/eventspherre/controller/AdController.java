package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ads")  // Base URL for all ad-related operations
public class AdController {

    private final AdService adService;

    @Autowired
    public AdController(AdService adService) {
        this.adService = adService;
    }

    // Create a new ad
    @PostMapping
    public ResponseEntity<Ad> createAd(@RequestBody Ad ad) {
        Ad createdAd = adService.createAd(ad);
        return new ResponseEntity<>(createdAd, HttpStatus.CREATED);  // 201 Created
    }

    // Get an ad by its ID
    @GetMapping("/{id}")
    public ResponseEntity<Ad> getAd(@PathVariable Long id) {
        Ad ad = adService.getAd(id);
        return new ResponseEntity<>(ad, HttpStatus.OK);  // 200 OK
    }

    // Get all ads
    @GetMapping
    public ResponseEntity<List<Ad>> getAllAds() {
        List<Ad> ads = adService.getAllAds();
        return new ResponseEntity<>(ads, HttpStatus.OK);  // 200 OK
    }

    // Update an existing ad
    @PutMapping("/{id}")
    public ResponseEntity<Ad> updateAd(@PathVariable Long id, @RequestBody Ad ad) {
        Ad updatedAd = adService.updateAd(id, ad);
        return new ResponseEntity<>(updatedAd, HttpStatus.OK);  // 200 OK
    }

    // Delete an ad
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);  // 204 No Content
    }
}
