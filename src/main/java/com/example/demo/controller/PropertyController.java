package com.example.demo.controller;

import com.example.demo.dto.PropertyDTO; // Import the DTO
import com.example.demo.model.Property;
import com.example.demo.model.User; // Import User for owner details in DTO conversion
import com.example.demo.model.enums.PropertyType;
import com.example.demo.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections; // Import Collections for emptySet
import java.util.HashSet;     // Import HashSet for defensive copy
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Import Collectors

@RestController
@RequestMapping("/api/properties") // Base path for general property access
@CrossOrigin(origins = "http://localhost:3001")
public class PropertyController {

    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

    @Autowired private PropertyService propertyService;

    // --- GET for Search/Listing (Returns List<PropertyDTO>) ---
    // Accessible to any authenticated user
    @GetMapping
    public ResponseEntity<List<PropertyDTO>> searchProperties( // Return specific DTO list type
                                                               @RequestParam(required = false) PropertyType type,
                                                               @RequestParam(required = false) String city,
                                                               @RequestParam(required = false) BigDecimal minPrice,
                                                               @RequestParam(required = false) BigDecimal maxPrice,
                                                               @RequestParam(required = false) Integer minBedrooms,
                                                               @RequestParam(required = false) Integer minBathrooms) {
        logger.info("Received property search request with filters - Type: {}, City: {}, Price: {}-{}, Beds: {}, Baths: {}",
                type, city, minPrice, maxPrice, minBedrooms, minBathrooms);
        try {
            List<Property> properties = propertyService.searchProperties(type, city, minPrice, maxPrice, minBedrooms, minBathrooms);
            // Convert List<Property> to List<PropertyDTO>
            List<PropertyDTO> propertyDTOs = properties.stream()
                    .map(this::convertToDto) // Use helper method
                    .collect(Collectors.toList());
            logger.debug("Returning {} properties after filtering.", propertyDTOs.size());
            return ResponseEntity.ok(propertyDTOs);
        } catch (Exception e) {
            logger.error("Error searching properties: {}", e.getMessage(), e);
            // Return an empty list with an error status (or could return an error object)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // --- GET by ID (Returns PropertyDTO) ---
    // Accessible to any authenticated user
    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> getPropertyById(@PathVariable Long id) { // Return specific DTO type
        logger.info("Received request for property ID: {}", id);
        // Convert Optional<Property> to Optional<PropertyDTO>
        return propertyService.findPropertyById(id)
                .map(this::convertToDto) // Convert found property to DTO
                .map(ResponseEntity::ok) // Wrap DTO in ResponseEntity<PropertyDTO>
                .orElseGet(() -> {
                    logger.warn("Property not found with ID: {}", id);
                    return ResponseEntity.notFound().build(); // Handle not found
                });
    }

    // --- POST to Create (Still accepts Property entity/CreateDTO, returns PropertyDTO) ---
    // Accessible to any authenticated user (service layer determines owner)
    @PostMapping
    public ResponseEntity<?> createProperty(@RequestBody Property property) { // Could use a specific CreatePropertyDTO
        logger.info("Received request to create property for address: {}", property.getAddress());
        // Consider adding @Valid annotation and validation constraints to a DTO here
        // Basic manual validation:
        if (property.getAddress() == null || property.getAddress().isBlank() ||
                property.getCity() == null || property.getCity().isBlank() ||
                property.getState() == null || property.getState().isBlank() ||
                property.getPostalCode() == null || property.getPostalCode().isBlank() ||
                property.getPrice() == null ||
                property.getType() == null ||
                property.getBedrooms() == null ||
                property.getBathrooms() == null) {
            logger.warn("Property creation failed: Missing required fields.");
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields."));
        }
        try {
            Property createdProperty = propertyService.createProperty(property);
            logger.info("Property created with ID: {}", createdProperty.getId());
            // Convert created Property entity to DTO before returning
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(createdProperty));
        } catch (IllegalStateException | UsernameNotFoundException e) { // Catch auth errors from service
            logger.warn("Property creation failed due to authentication issue: {}", e.getMessage());
            // Return 401 or 403 depending on specific cause if distinguishable
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) { // Catch other errors like DB constraints during save
            logger.error("Error creating property: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error creating property. Details logged."));
        }
    }

    // --- Helper Method: Convert Property Entity to PropertyDTO ---
    // Ensures consistent data structure is sent to the frontend
    private PropertyDTO convertToDto(Property property) {
        if (property == null) {
            return null;
        }
        PropertyDTO dto = new PropertyDTO();
        // Map basic fields
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
        dto.setImageUrls(property.getImageUrls()); // Include image URLs string
        dto.setCreatedAt(property.getCreatedAt());
        dto.setUpdatedAt(property.getUpdatedAt());

        // Map Amenities (handle null Set from entity defensively)
        dto.setAmenities(property.getAmenities() != null ? new HashSet<>(property.getAmenities()) : Collections.emptySet());

        // Map Owner Info (handle null Owner object defensively)
        User owner = property.getOwner(); // Get the possibly LAZY loaded owner
        if (owner != null) {
            // Assuming User entity has getId(), getName(), getEmail()
            try {
                // Access owner details (might trigger fetch if LAZY)
                dto.setOwnerId(owner.getId());
                dto.setOwnerName(owner.getName());
                dto.setOwnerEmail(owner.getEmail()); // Ensure DTO has this field/setter
            } catch (org.hibernate.LazyInitializationException e) {
                logger.warn("Could not lazy-load owner details for property ID {} during DTO conversion. Sending only ID.", property.getId());
                // Fallback: Use ownerId if available directly, set others to N/A
                dto.setOwnerId(property.getOwnerId()); // Use direct ID from Property entity
                dto.setOwnerName("Owner details not loaded");
                dto.setOwnerEmail("N/A");
            }
        } else {
            // If owner object itself is null
            logger.warn("Owner object was null for property ID {} during DTO conversion.", property.getId());
            dto.setOwnerId(property.getOwnerId()); // Use direct ID if available
            dto.setOwnerName("N/A");
            dto.setOwnerEmail("N/A");
        }

        return dto;
    }
    // --- End Helper Method ---

    // PUT, DELETE, Image Upload endpoints are now in OwnerPropertyController
    /*
    @PutMapping("/{id}")
    // ... removed ...

    @DeleteMapping("/{id}")
    // ... removed ...

    @PostMapping("/{id}/images")
    // ... removed ...
    */
}