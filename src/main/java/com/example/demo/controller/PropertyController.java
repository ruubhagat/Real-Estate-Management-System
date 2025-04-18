package com.example.demo.controller;

import com.example.demo.model.Property;
import com.example.demo.model.enums.PropertyType;
import com.example.demo.service.PropertyService;
import com.example.demo.service.FileStorageService; // Import FileStorageService
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import java.math.BigDecimal;
import java.util.ArrayList;
// import java.util.Arrays; // Not needed currently
import java.util.List;
import java.util.Map;
import java.util.Optional;
// import java.util.stream.Collectors; // Not needed currently

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins = "http://localhost:3001")
public class PropertyController {

    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

    @Autowired private PropertyService propertyService;
    @Autowired private FileStorageService fileStorageService; // Inject FileStorageService

    // --- GET for Search/Listing ---
    @GetMapping
    public ResponseEntity<?> searchProperties(
            @RequestParam(required = false) PropertyType type, @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minBedrooms, @RequestParam(required = false) Integer minBathrooms) {
        logger.info("Received property search request");
        try {
            List<Property> properties = propertyService.searchProperties(type, city, minPrice, maxPrice, minBedrooms, minBathrooms);
            // Return entities - DTOs recommended
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            logger.error("Error searching properties: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error searching properties."));
        }
    }

    // --- GET by ID ---
    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable Long id) {
        logger.info("Received request for property ID: {}", id);
        return propertyService.findPropertyById(id)
                .map(ResponseEntity::ok) // Return entity
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- POST to Create ---
    @PostMapping
    public ResponseEntity<?> createProperty(@RequestBody Property property) {
        logger.info("Received request to create property");
        // Uses manual getters
        if (property.getAddress() == null || property.getPrice() == null || property.getType() == null || property.getCity() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields (address, city, price, type)."));
        }
        try {
            Property createdProperty = propertyService.createProperty(property);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProperty); // Return entity
        } catch (Exception e) {
            logger.error("Error creating property: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error creating property."));
        }
    }

    // --- PUT to Update ---
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProperty(@PathVariable Long id, @RequestBody Property propertyData) {
        logger.info("Received request to update property ID: {}", id);
        try {
            // This updates general fields BUT NOT imageUrls
            return propertyService.updateProperty(id, propertyData)
                    .map(ResponseEntity::ok) // Return entity
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating property: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error updating property."));
        }
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProperty(@PathVariable Long id) {
        logger.info("Received request to delete property ID: {}", id);
        try {
            boolean deleted = propertyService.deleteProperty(id);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting property: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error deleting property."));
        }
    }

    // --- POST Endpoint for Image Upload ---
    /**
     * Uploads one or more images for a specific property.
     * Appends the generated filenames to the property's imageUrls field.
     * Requires authentication and ownership/admin rights (checked by service).
     * @param id The ID of the property to add images to.
     * @param files The image files being uploaded (@RequestParam named "files").
     * @return Success message with filenames or error message.
     */
    @PostMapping("/{id}/images")
    public ResponseEntity<?> uploadPropertyImages(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
        logger.info("Received request to upload {} images for property ID: {}", files.length, id);

        // 1. Find the property first to get existing URLs (optional, service finds it again)
        Optional<Property> propertyOpt = propertyService.findPropertyById(id);
        if (propertyOpt.isEmpty()) {
            // No need to check permissions if property doesn't exist
            return ResponseEntity.notFound().build();
        }
        Property currentProperty = propertyOpt.get(); // Get current property for existing URLs

        // 2. Process and store each valid file using FileStorageService
        List<String> uploadedFileNames = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    // Basic content type check (add more robust validation)
                    String contentType = file.getContentType();
                    if (contentType == null || (!contentType.startsWith("image/"))) { // Simplified check
                        logger.warn("Skipping non-image file upload: {}", file.getOriginalFilename());
                        continue; // Skip this file
                    }
                    // Store file and get back the unique filename generated
                    String fileName = fileStorageService.storeFile(file);
                    uploadedFileNames.add(fileName);
                }
            }
        } catch (RuntimeException e) { // Catch potential storage errors
            logger.error("Error storing file for property {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to store one or more files. " + e.getMessage()));
        }

        // 3. Check if any valid files were actually uploaded
        if (uploadedFileNames.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No valid image files were uploaded or processed."));
        }

        // 4. Prepare the new imageUrls string (append to existing ones)
        String existingUrlString = currentProperty.getImageUrls(); // Use manual getter
        String newUrlString;
        String namesToStore = String.join(",", uploadedFileNames); // Comma-separate the new filenames

        if (existingUrlString != null && !existingUrlString.isBlank()) {
            newUrlString = existingUrlString + "," + namesToStore;
        } else {
            newUrlString = namesToStore;
        }

        // 5. Call the dedicated service method to update only the image URLs
        try {
            propertyService.updateImageUrls(id, newUrlString); // Call the correct service method
            logger.info("Successfully updated image filenames for property ID: {}", id);
            // Return success response with the names of the files saved
            return ResponseEntity.ok(Map.of(
                    "message", "Images uploaded and property updated successfully.",
                    "imageFilenames", uploadedFileNames
            ));
        } catch (IllegalArgumentException e) { // Catch property not found from service
            logger.warn("Update image URLs failed: Property not found with ID: {}", id);
            return ResponseEntity.notFound().build(); // Should ideally not happen if checked above, but good practice
        } catch (SecurityException e) { // Catch permission denied from service
            logger.warn("Unauthorized attempt to upload images for property ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) { // Catch other potential DB save errors
            logger.error("Error saving property after image upload for ID {}: {}", id, e.getMessage(), e);
            // TODO: Consider deleting the files just uploaded from the filesystem if DB save fails (rollback)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update property with image paths."));
        }
    }
}