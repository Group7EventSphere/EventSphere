package id.ac.ui.cs.advprog.eventspherre.service;

import id.ac.ui.cs.advprog.eventspherre.model.Ad;
import id.ac.ui.cs.advprog.eventspherre.model.UnauthorizedAccessException;

public class AdService {

    private final String currentUserRole;  // Store the role of the current user

    // Constructor to set the current user role (e.g., ADMIN or USER)
    public AdService(String role) {
        this.currentUserRole = role;
    }

    // Method to create an ad (only accessible by admins)
    public Ad createAd(Ad ad) {
        if ("ADMIN".equalsIgnoreCase(currentUserRole)) {
            // Logic to save ad (in-memory storage or database)
            return ad;  // For simplicity, we return the ad
        } else {
            // Throw exception if the user is not an admin
            throw new UnauthorizedAccessException("Only admins can create ads.");
        }
    }

    // Method to update an ad (only accessible by admins)
    public Ad updateAd(Long id, Ad updatedAd) {
        if ("ADMIN".equalsIgnoreCase(currentUserRole)) {
            // Update ad logic
            updatedAd.setId(id);
            return updatedAd;  // For simplicity, we return the updated ad
        } else {
            // Throw exception if the user is not an admin
            throw new UnauthorizedAccessException("Only admins can edit ads.");
        }
    }

    // Method to view an ad (accessible by all users)
    public Ad viewAd(Long id) {
        // Return a sample ad (or retrieve from the database)
        return new Ad(id, "Sample Ad", "Sample description", "image.jpg", currentUserRole);
    }
}
