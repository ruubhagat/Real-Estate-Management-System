package com.example.demo.service;

import com.example.demo.model.Property;
import com.example.demo.model.User;
import com.example.demo.model.enums.PropertyStatus;
import com.example.demo.model.enums.PropertyType;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PropertyService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyService.class);

    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private UserRepository userRepository;

    // --- Read Operations ---
    @Transactional(readOnly = true)
    public Optional<Property> findPropertyById(Long id) {
        logger.debug("Finding property by ID: {}", id);
        // Fetch eagerly with owner and amenities if needed frequently, or rely on EAGER fetch type
        // Consider adding a method with specific joins if performance becomes an issue
        return propertyRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Property> searchProperties(
            PropertyType type, String city, BigDecimal minPrice, BigDecimal maxPrice,
            Integer minBedrooms, Integer minBathrooms) {
        PropertyStatus status = PropertyStatus.AVAILABLE;
        String cityFilter = (city != null && city.isBlank()) ? null : city;
        logger.debug("Searching properties with criteria - Status: {}, Type: {}, City: {}, MinPrice: {}, MaxPrice: {}, MinBeds: {}, MinBaths: {}",
                status, type, cityFilter, minPrice, maxPrice, minBedrooms, minBathrooms);
        return propertyRepository.findPropertiesByCriteria(status, type, cityFilter, minPrice, maxPrice, minBedrooms, minBathrooms);
    }

    @Transactional(readOnly = true)
    public List<Property> findAllAvailableProperties() { /* ... */ return propertyRepository.findByStatus(PropertyStatus.AVAILABLE); }
    @Transactional(readOnly = true)
    public List<Property> findAllPropertiesAdmin() { /* ... */ return propertyRepository.findAll(); }


    // --- Write Operations ---
    @Transactional
    public Property createProperty(Property property) {
        String ownerEmail = getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User must be authenticated to create a property"));
        logger.debug("Attempting to create property for user: {}", ownerEmail);
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + ownerEmail));

        property.setOwner(owner);
        if (property.getStatus() == null) { property.setStatus(PropertyStatus.AVAILABLE); }
        // Amenities set directly from request if included
        // @PrePersist handles createdAt

        Property savedProperty = propertyRepository.save(property);
        logger.info("Property created with ID {} for owner {}", savedProperty.getId(), ownerEmail);
        return savedProperty;
    }

    // --- Update Property with Detailed Logging ---
    @Transactional
    public Optional<Property> updateProperty(Long id, Property updatedPropertyData) {
        logger.debug("Attempting to update property data for ID: {}", id);
        Optional<Property> existingPropertyOpt = propertyRepository.findById(id);

        if (existingPropertyOpt.isEmpty()) {
            logger.warn("Update failed: Property not found with ID: {}", id);
            return Optional.empty();
        }

        Property existingProperty = existingPropertyOpt.get();

        // Update standard fields
        existingProperty.setAddress(updatedPropertyData.getAddress());
        existingProperty.setCity(updatedPropertyData.getCity());
        existingProperty.setState(updatedPropertyData.getState());
        existingProperty.setPostalCode(updatedPropertyData.getPostalCode());
        existingProperty.setPrice(updatedPropertyData.getPrice());
        existingProperty.setBedrooms(updatedPropertyData.getBedrooms());
        existingProperty.setBathrooms(updatedPropertyData.getBathrooms());
        existingProperty.setAreaSqft(updatedPropertyData.getAreaSqft());
        existingProperty.setDescription(updatedPropertyData.getDescription());
        existingProperty.setType(updatedPropertyData.getType());
        existingProperty.setStatus(updatedPropertyData.getStatus());

        // --- Update the Amenities Set with Logging ---
        Set<String> incomingAmenities = updatedPropertyData.getAmenities();
        logger.info("[Service Update] Property ID: {}. Amenities received in Request DTO/Entity: {}", id, incomingAmenities);
        logger.info("[Service Update] Property ID: {}. Amenities currently on Entity before clear: {}", id, existingProperty.getAmenities());

        if (incomingAmenities != null) {
            // Modify the existing Set managed by Hibernate
            existingProperty.getAmenities().clear();
            existingProperty.getAmenities().addAll(incomingAmenities);
            logger.info("[Service Update] Property ID: {}. Amenities on Entity AFTER update logic: {}", id, existingProperty.getAmenities());
        } else {
            existingProperty.getAmenities().clear();
            logger.info("[Service Update] Property ID: {}. Clearing all amenities as incoming was null.", id);
        }
        // --- End Amenities Update ---

        // @PreUpdate handles updatedAt
        logger.info("[Service Update] Property ID: {}. Attempting to SAVE entity with amenities: {}", id, existingProperty.getAmenities());
        Property savedProperty = propertyRepository.save(existingProperty);

        // Fetch again immediately after save to verify persistence (within same transaction)
        Property checkProperty = propertyRepository.findById(id).orElse(null);
        logger.info("[Service Update] Property ID: {}. Amenities on Entity immediately AFTER save (refetched): {}", id, checkProperty != null ? checkProperty.getAmenities() : "ENTITY NOT FOUND AFTER SAVE!?");

        logger.info("Property data (including amenities) updated successfully for ID: {}", id);
        return Optional.of(savedProperty); // Return the instance returned by save
    }
    // --- End Update Property ---


    @Transactional
    public Property updateImageUrls(Long propertyId, String newImageUrlsString) { /* ... (no changes needed here) ... */
        logger.debug("Attempting to update image URLs for property ID: {}", propertyId);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + propertyId));
        property.setImageUrls(newImageUrlsString);
        Property savedProperty = propertyRepository.save(property); // Triggers @PreUpdate
        logger.info("Image URLs updated successfully for property ID: {}", propertyId);
        return savedProperty;
    }

    @Transactional
    public boolean deleteProperty(Long id) { /* ... (no changes needed here) ... */
        logger.debug("Attempting to delete property with ID: {}", id);
        Optional<Property> propertyOpt = propertyRepository.findById(id);
        if (propertyOpt.isEmpty()) { logger.warn("Delete failed: Property not found with ID: {}", id); return false; }
        propertyRepository.deleteById(id);
        logger.info("Property deleted successfully for ID: {}", id);
        return true;
    }

    // --- Method for @PreAuthorize ownership check ---
    @Transactional(readOnly = true)
    public boolean checkOwnership(Long propertyId) { /* ... (no changes needed here) ... */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) { return false; }
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername).orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + currentUsername));
        Long currentUserId = currentUser.getId();
        Property property = propertyRepository.findById(propertyId).orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
        Long propertyOwnerId = property.getOwnerId();
        if (propertyOwnerId == null && property.getOwner() != null) { propertyOwnerId = property.getOwner().getId(); }
        if (propertyOwnerId == null) { logger.error("Property ID {} has null ownerId.", propertyId); return false; }
        boolean isOwner = propertyOwnerId.equals(currentUserId);
        logger.debug("[checkOwnership] Property ID: {}, Current User ID: {}, Owner ID: {}, Is Owner?: {}", propertyId, currentUserId, propertyOwnerId, isOwner);
        return isOwner;
    }

    // --- Helper Method to get current user ---
    private Optional<String> getCurrentUsername() { /* ... (no changes needed here) ... */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) { return Optional.empty(); }
        return Optional.ofNullable(authentication.getName());
    }

    // Keep verifyOwnershipOrAdmin if needed by Admins elsewhere
    private void verifyOwnershipOrAdmin(Property property) { /* ... */ }
}