package com.example.demo.controller;

import com.example.demo.model.Property; // Assuming returning entity for simplicity
import com.example.demo.service.PropertyService;
// Import User service/repo if managing users
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin") // Base path for admin actions
@CrossOrigin(origins = "http://localhost:3001") // Adjust if needed
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private PropertyService propertyService;
    // Autowire UserService, BookingService etc. as needed

    /**
     * Endpoint for ADMIN to fetch all properties (including non-available).
     * @return List of all properties or error.
     */
    @GetMapping("/properties")
    @PreAuthorize("hasRole('ADMIN')") // Secure endpoint for ADMIN role only
    public ResponseEntity<List<Property>> getAllProperties() {
        logger.info("Admin request received: getAllProperties");
        try {
            List<Property> properties = propertyService.findAllPropertiesAdmin();
            // TODO: Convert to DTOs is highly recommended
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            logger.error("Admin: Error fetching all properties: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    /**
     * Endpoint for ADMIN to delete any property.
     * Note: Uses the existing service method which checks ownership OR Admin.
     * If separate admin delete logic was needed, a new service method would be better.
     * @param id The ID of the property to delete.
     * @return No Content on success, or error status.
     */
    @DeleteMapping("/properties/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Secure endpoint
    public ResponseEntity<?> deleteAnyProperty(@PathVariable Long id) {
        logger.info("Admin request received: deleteAnyProperty for ID: {}", id);
        try {
            // Reuse existing service method - verifyOwnershipOrAdmin handles ADMIN permission
            boolean deleted = propertyService.deleteProperty(id);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (SecurityException e) { // Should not happen if PreAuthorize works, but good practice
            logger.warn("Admin: Forbidden attempt to delete property ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Admin: Error deleting property {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal error deleting property."));
        }
    }

    // TODO: Add endpoints for managing users, all bookings, etc.
    // Example:
    // @GetMapping("/users")
    // @PreAuthorize("hasRole('ADMIN')")
    // public ResponseEntity<List<User>> getAllUsers() { ... }

}