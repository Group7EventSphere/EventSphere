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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ads")
public class AdRestController {

    private final AdService adService;
    private final ImageStorageService imageStorageService;

    @Autowired
    public AdRestController(AdService adService,
                        ImageStorageService imageStorageService) {
        this.adService = adService;
        this.imageStorageService = imageStorageService;
    }

    // Create Ad via DTO
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdRequestDto> createAd(@RequestBody AdRequestDto dto) {
        Ad ad = Ad.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .build();
        Ad saved = adService.createAd(ad);
        AdRequestDto result = toDto(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // Get single Ad
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdRequestDto> getAd(@PathVariable Long id) {
        Ad ad = adService.getAd(id);
        if (ad == null) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(toDto(ad));
    }

    // Get all Ads
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Ad>> getAllAds() {
        List<Ad> list = adService.getAllAds();
        return ResponseEntity.ok(list);
    }

    // Update Ad via DTO
    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdRequestDto> updateAd(
            @PathVariable Long id,
            @RequestBody AdRequestDto dto
    ) {
        Ad ad = Ad.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .build();
        Ad updated = adService.updateAd(id, ad);
        if (updated == null) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(toDto(updated));
    }

    // Delete Ad
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return ResponseEntity.noContent().build();
    }

    // helper: Entity → DTO
    private AdRequestDto toDto(Ad ad) {
        return AdRequestDto.builder()
                .title(ad.getTitle())
                .description(ad.getDescription())
                .imageUrl(ad.getImageUrl())
                .build();
    }
}
