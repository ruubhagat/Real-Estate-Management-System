package com.example.demo.controller;

import com.example.demo.model.Property;
import com.example.demo.model.enums.PropertyType;
import com.example.demo.service.PropertyService;
// Remove FileStorageService and MultipartFile imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import for catch block
import org.springframework.web.bind.annotation.*;
// Remove other unused imports

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
// Remove Optional if not needed

@RestController
@RequestMapping("/api/properties") // Keep the base path for general access
@CrossOrigin(origins = "http://localhost:3001")
public class PropertyController {

    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

    @Autowired private PropertyService propertyService;

    // --- GET for Search/Listing (Keep) ---
    // Accessible to any authenticated user
    @GetMapping
    public ResponseEntity<?> searchProperties(
            @RequestParam(required = false) PropertyType type, @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minBedrooms, @RequestParam(required = false) Integer minBathrooms) {
        logger.info("Received property search request");
        try {
            List<Property> properties = propertyService.searchProperties(type, city, minPrice, maxPrice, minBedrooms, minBathrooms);
            // TODO: Convert List<Property> to List<PropertyDTO> before returning
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            logger.error("Error searching properties: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error searching properties."));
        }
    }

    // --- GET by ID (Keep) ---
    // Accessible to any authenticated user
    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable Long id) {
        logger.info("Received request for property ID: {}", id);
        // TODO: Convert Property to PropertyDTO before returning
        return propertyService.findPropertyById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- POST to Create (Keep - Service assigns owner based on authentication) ---
    // Accessible to any authenticated user (service layer determines owner)
    @PostMapping
    public ResponseEntity<?> createProperty(@RequestBody Property property) {
        logger.info("Received request to create property");
        // More robust validation including all non-nullable fields
        if (property.getAddress() == null || property.getAddress().isBlank() ||
                property.getCity() == null || property.getCity().isBlank() ||
                property.getState() == null || property.getState().isBlank() || // Added state
                property.getPostalCode() == null || property.getPostalCode().isBlank() || // Added postalCode
                property.getPrice() == null ||
                property.getType() == null ||
                property.getBedrooms() == null || // Added bedrooms
                property.getBathrooms() == null) { // Added bathrooms
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields (address, city, state, postalCode, price, type, bedrooms, bathrooms)."));
        }
        try {
            Property createdProperty = propertyService.createProperty(property);
            // TODO: Convert createdProperty to PropertyDTO before returning
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProperty);
        } catch (IllegalStateException | UsernameNotFoundException e) { // Catch auth errors from service
            logger.warn("Property creation failed due to authentication issue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) { // Catch other errors like DB constraints
            logger.error("Error creating property: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error creating property. Details logged."));
        }
    }

    // --- PUT to Update (REMOVED - Moved to OwnerPropertyController) ---
    // --- DELETE (REMOVED - Moved to OwnerPropertyController) ---
    // --- POST Endpoint for Image Upload (REMOVED - Moved to OwnerPropertyController) ---

}