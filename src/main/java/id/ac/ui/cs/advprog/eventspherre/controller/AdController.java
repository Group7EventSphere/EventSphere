package id.ac.ui.cs.advprog.eventspherre.controller;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    private final AdService adService;

    @Autowired
    public AdController(AdService adService) {
        this.adService = adService;
    }

    // CREATE (JSON body)
    @PostMapping
    public ResponseEntity<Ad> createAd(@RequestBody Ad ad) {
        Ad created = adService.createAd(ad);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // READ one
    @GetMapping("/{id}")
    public ResponseEntity<Ad> getAd(@PathVariable Long id) {
        Ad ad = adService.getAd(id);
        // test expects 200 OK and empty body if not found
        return ad != null
                ? ResponseEntity.ok(ad)
                : ResponseEntity.ok().body(null);
    }

    // READ all
    @GetMapping
    public ResponseEntity<List<Ad>> getAllAds() {
        List<Ad> ads = adService.getAllAds();
        return ResponseEntity.ok(ads);
    }

    // UPDATE (JSON body)
    @PutMapping("/{id}")
    public ResponseEntity<Ad> updateAd(
            @PathVariable Long id,
            @RequestBody Ad ad
    ) {
        Ad updated = adService.updateAd(id, ad);
        // test expects 200 OK and empty body if not found
        return updated != null
                ? ResponseEntity.ok(updated)
                : ResponseEntity.ok().body(null);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return ResponseEntity.noContent().build();
    }
}
