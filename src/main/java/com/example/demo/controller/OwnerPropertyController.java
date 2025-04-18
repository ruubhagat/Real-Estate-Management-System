package com.example.demo.controller;

import com.example.demo.model.Property;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/owner/properties") // Base path for owner-specific property actions
@CrossOrigin(origins = "http://localhost:3001") // Adjust CORS as needed
public class OwnerPropertyController {

    private static final Logger logger = LoggerFactory.getLogger(OwnerPropertyController.class);

    @Autowired private PropertyService propertyService;
    @Autowired private FileStorageService fileStorageService;

    // --- PUT to Update Property (Owner Only) ---
    @PutMapping("/{id}")
    @PreAuthorize("@propertyService.checkOwnership(#id)") // Check ownership before execution
    public ResponseEntity<?> updateMyProperty(@PathVariable Long id, @RequestBody Property propertyData) {
        logger.info("Owner request received to update property ID: {}", id);
        try {
            // Service method already handles not found internally
            return propertyService.updateProperty(id, propertyData)
                    .map(ResponseEntity::ok) // Return updated property entity/DTO
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) { // Catch potential not found from service layer if logic changes
            logger.warn("Owner Update failed: Property not found with ID: {} (Possible race condition or direct call issue)", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) { // Catch unexpected errors
            logger.error("Owner Error: Error updating property {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error updating property."));
        }
    }

    // --- DELETE Property (Owner Only) ---
    @DeleteMapping("/{id}")
    @PreAuthorize("@propertyService.checkOwnership(#id)") // Check ownership before execution
    public ResponseEntity<?> deleteMyProperty(@PathVariable Long id) {
        logger.info("Owner request received to delete property ID: {}", id);
        try {
            // Service method handles not found internally
            boolean deleted = propertyService.deleteProperty(id);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) { // Catch potential not found from service layer if logic changes
            logger.warn("Owner Delete failed: Property not found with ID: {} (Possible race condition or direct call issue)", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) { // Catch unexpected errors
            logger.error("Owner Error: Error deleting property {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error deleting property."));
        }
    }

    // --- POST Endpoint for Image Upload (Owner Only) ---
    @PostMapping("/{id}/images")
    @PreAuthorize("@propertyService.checkOwnership(#id)") // Check ownership before execution
    public ResponseEntity<?> uploadMyPropertyImages(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
        logger.info("Owner request received to upload {} images for property ID: {}", files.length, id);

        // 1. Find the property first to get existing URLs and ensure it exists
        // Although @PreAuthorize checked ownership (implying existence), checking again is safer.
        Optional<Property> propertyOpt = propertyService.findPropertyById(id);
        if (propertyOpt.isEmpty()) {
            logger.warn("Owner image upload failed: Property not found with ID: {} (despite @PreAuthorize passing?)", id);
            return ResponseEntity.notFound().build();
        }
        Property currentProperty = propertyOpt.get();

        // 2. Process and store each valid file
        List<String> uploadedFileNames = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String contentType = file.getContentType();
                    if (contentType == null || (!contentType.startsWith("image/"))) {
                        logger.warn("Owner image upload: Skipping non-image file: {}", file.getOriginalFilename());
                        continue;
                    }
                    String fileName = fileStorageService.storeFile(file);
                    uploadedFileNames.add(fileName);
                }
            }
        } catch (RuntimeException e) { // Catch storage errors
            logger.error("Owner Error: Error storing file for property {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to store one or more files. " + e.getMessage()));
        }

        // 3. Check if any valid files were actually uploaded
        if (uploadedFileNames.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No valid image files were uploaded or processed."));
        }

        // 4. Prepare the new imageUrls string
        String existingUrlString = currentProperty.getImageUrls();
        String namesToStore = String.join(",", uploadedFileNames);
        String newUrlString = (existingUrlString != null && !existingUrlString.isBlank())
                ? existingUrlString + "," + namesToStore
                : namesToStore;

        // 5. Call the service method to update only the image URLs
        try {
            propertyService.updateImageUrls(id, newUrlString); // Use the dedicated service method
            logger.info("Owner successfully updated image filenames for property ID: {}", id);
            return ResponseEntity.ok(Map.of(
                    "message", "Images uploaded and property updated successfully.",
                    "imageFilenames", uploadedFileNames
            ));
        } catch (IllegalArgumentException e) { // Catch property not found from service
            logger.warn("Owner update image URLs failed: Property not found with ID: {} (Possible race condition)", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) { // Catch other potential DB save errors
            logger.error("Owner Error: Error saving property after image upload for ID {}: {}", id, e.getMessage(), e);
            // Consider deleting the files just uploaded from the filesystem if DB save fails (rollback)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update property with image paths."));
        }
    }
}