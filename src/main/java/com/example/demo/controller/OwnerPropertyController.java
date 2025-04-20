package com.example.demo.controller;

import com.example.demo.dto.PropertyDTO; // Import DTO
import com.example.demo.model.Property;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Import for catch block
import org.springframework.security.access.prepost.PreAuthorize;
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
        // Use Property entity as request body for simplicity, or create a dedicated UpdateDTO
        logger.info("Owner request received to update property ID: {}", id);
        // --- VVV ADD LOGGING VVV ---
        // Log received data (including amenities) before passing to service
        logger.info("Received update payload in controller: Address={}, City={}, Amenities={}",
                propertyData.getAddress(), propertyData.getCity(), propertyData.getAmenities());
        // --- ^^^ END LOGGING ^^^ ---
        try {
            // Service method attempts the update
            return propertyService.updateProperty(id, propertyData)
                    // --- VVV Convert to DTO before sending response VVV ---
                    .map(updatedEntity -> ResponseEntity.ok(convertToDto(updatedEntity))) // Use helper/factory
                    // --- ^^^ End Conversion ^^^ ---
                    .orElseGet(() -> {
                        logger.warn("Owner Update failed: Property with ID {} not found by service.", id);
                        return ResponseEntity.notFound().build(); // Return 404 if service returns empty Optional
                    });
        } catch (AccessDeniedException e) { // Catch potential security exceptions if @PreAuthorize fails unexpectedly
            logger.warn("Owner Update Forbidden for property ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) { // Catch specific exceptions like property not found within service calls
            logger.warn("Owner Update failed for property ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build(); // Treat as Not Found
        } catch (Exception e) { // Catch unexpected errors
            logger.error("Owner Error: Unexpected error updating property {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error updating property."));
        }
    }

    // --- DELETE Property (Owner Only) ---
    @DeleteMapping("/{id}")
    @PreAuthorize("@propertyService.checkOwnership(#id)") // Check ownership before execution
    public ResponseEntity<?> deleteMyProperty(@PathVariable Long id) {
        logger.info("Owner request received to delete property ID: {}", id);
        try {
            boolean deleted = propertyService.deleteProperty(id);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            logger.warn("Owner Delete Forbidden for property ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) { // Catch property not found from service
            logger.warn("Owner Delete failed: Property not found with ID: {}", id);
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

        Optional<Property> propertyOpt = propertyService.findPropertyById(id);
        if (propertyOpt.isEmpty()) {
            logger.warn("Owner image upload failed: Property not found with ID: {} (despite @PreAuthorize passing?)", id);
            return ResponseEntity.notFound().build();
        }
        Property currentProperty = propertyOpt.get();

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
        } catch (RuntimeException e) {
            logger.error("Owner Error: Error storing file for property {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to store one or more files. " + e.getMessage()));
        }

        if (uploadedFileNames.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No valid image files were uploaded or processed."));
        }

        String existingUrlString = currentProperty.getImageUrls();
        String namesToStore = String.join(",", uploadedFileNames);
        String newUrlString = (existingUrlString != null && !existingUrlString.isBlank())
                ? existingUrlString + "," + namesToStore
                : namesToStore;

        try {
            propertyService.updateImageUrls(id, newUrlString);
            logger.info("Owner successfully updated image filenames for property ID: {}", id);
            return ResponseEntity.ok(Map.of(
                    "message", "Images uploaded and property updated successfully.",
                    "imageFilenames", uploadedFileNames
            ));
        } catch (AccessDeniedException e) { // Should be caught by @PreAuthorize, but belt-and-suspenders
            logger.warn("Owner update image URLs Forbidden for property ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) { // Catch property not found from service
            logger.warn("Owner update image URLs failed: Property not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Owner Error: Error saving property after image upload for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update property with image paths."));
        }
    }

    // --- Helper Method: Convert Property Entity to PropertyDTO ---
    // Duplicated here for now, consider moving to a shared utility or using MapStruct
    private PropertyDTO convertToDto(Property property) {
        if (property == null) {
            return null;
        }
        PropertyDTO dto = new PropertyDTO();
        dto.setId(property.getId());
        dto.setAddress(property.getAddress());
        dto.setCity(property.getCity());
        dto.setState(property.getState());
        dto.setPostalCode(property.getPostalCode());
        dto.setPrice(property.getPrice());
        dto.setBedrooms(property.getBedrooms());
        dto.setBathrooms(property.getBathrooms());
        dto.setAreaSqft(property.getAreaSqft());
        dto.setDescription(property.getDescription());
        dto.setType(property.getType());
        dto.setStatus(property.getStatus());
        dto.setImageUrls(property.getImageUrls());
        dto.setCreatedAt(property.getCreatedAt());
        dto.setUpdatedAt(property.getUpdatedAt());
        dto.setAmenities(property.getAmenities()); // Include amenities

        if (property.getOwner() != null) {
            dto.setOwnerId(property.getOwner().getId());
            dto.setOwnerName(property.getOwner().getName());
            dto.setOwnerEmail(property.getOwner().getEmail());
        }
        return dto;
    }
}