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
    public List<Property> findAllAvailableProperties() {
        logger.debug("Finding all available properties");
        return propertyRepository.findByStatus(PropertyStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public List<Property> findAllPropertiesAdmin() {
        logger.debug("Admin request: Finding all properties.");
        return propertyRepository.findAll();
    }


    // --- Write Operations ---
    @Transactional
    public Property createProperty(Property property) {
        String ownerEmail = getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User must be authenticated to create a property"));
        logger.debug("Attempting to create property for user: {}", ownerEmail);
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + ownerEmail));

        property.setOwner(owner);

        if (property.getStatus() == null) {
            property.setStatus(PropertyStatus.AVAILABLE);
            logger.debug("Property status was null, setting default status to: {}", property.getStatus());
        }
        // Property entity's @PrePersist handles createdAt

        Property savedProperty = propertyRepository.save(property);
        logger.info("Property created with ID {} for owner {}", savedProperty.getId(), ownerEmail);
        return savedProperty;
    }

    @Transactional
    public Optional<Property> updateProperty(Long id, Property updatedPropertyData) {
        // Note: Permission check (@PreAuthorize) is now handled in the controller calling this
        logger.debug("Attempting to update general property data for ID: {}", id);
        Optional<Property> existingPropertyOpt = propertyRepository.findById(id);
        if (existingPropertyOpt.isEmpty()) {
            logger.warn("Update failed: Property not found with ID: {}", id);
            // Return empty optional, controller should handle this as 404
            return Optional.empty();
        }
        Property existingProperty = existingPropertyOpt.get();
        // No need to call verifyOwnershipOrAdmin here if @PreAuthorize is used

        // Update fields (imageUrls updated separately)
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
        // Entity's @PreUpdate handles updatedAt

        Property savedProperty = propertyRepository.save(existingProperty);
        logger.info("Property general data updated successfully for ID: {}", id);
        return Optional.of(savedProperty);
    }

    @Transactional
    public Property updateImageUrls(Long propertyId, String newImageUrlsString) {
        // Note: Permission check (@PreAuthorize) is now handled in the controller calling this
        logger.debug("Attempting to update image URLs for property ID: {}", propertyId);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> {
                    logger.warn("Update image URLs failed: Property not found with ID: {}", propertyId);
                    return new IllegalArgumentException("Property not found with ID: " + propertyId);
                });
        // No need to call verifyOwnershipOrAdmin here if @PreAuthorize is used

        property.setImageUrls(newImageUrlsString);
        // Entity's @PreUpdate handles updatedAt
        Property savedProperty = propertyRepository.save(property);
        logger.info("Image URLs updated successfully for property ID: {}", propertyId);
        return savedProperty;
    }


    @Transactional
    public boolean deleteProperty(Long id) {
        // Note: Permission check (@PreAuthorize) is now handled in the controller calling this
        logger.debug("Attempting to delete property with ID: {}", id);
        Optional<Property> propertyOpt = propertyRepository.findById(id);
        if (propertyOpt.isEmpty()) {
            logger.warn("Delete failed: Property not found with ID: {}", id);
            return false; // Controller handles as 404
        }
        // No need to call verifyOwnershipOrAdmin here if @PreAuthorize is used
        propertyRepository.deleteById(id);
        logger.info("Property deleted successfully for ID: {}", id);
        return true;
    }

    // --- NEW Method for @PreAuthorize check ---
    /**
     * Checks if the currently authenticated user owns the property with the given ID.
     * Intended for use with @PreAuthorize annotations.
     * @param propertyId The ID of the property to check.
     * @return true if the current user owns the property, false otherwise.
     * @throws UsernameNotFoundException If the authenticated user cannot be found.
     * @throws IllegalArgumentException If the property cannot be found.
     */
    @Transactional(readOnly = true) // Good practice for read operations
    public boolean checkOwnership(Long propertyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("[checkOwnership] Attempted check without authentication for property ID: {}", propertyId);
            return false; // @PreAuthorize will typically deny access if this returns false
        }
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found in DB: " + currentUsername));
        Long currentUserId = currentUser.getId();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + propertyId));

        Long propertyOwnerId = property.getOwnerId(); // Use direct ID getter
        if (propertyOwnerId == null && property.getOwner() != null) { // Fallback
            propertyOwnerId = property.getOwner().getId();
        }

        if (propertyOwnerId == null) {
            logger.error("[checkOwnership] Property ID {} has null ownerId.", propertyId);
            return false; // Cannot verify ownership if owner ID is missing
        }

        boolean isOwner = propertyOwnerId.equals(currentUserId);
        logger.debug("[checkOwnership] Property ID: {}, Current User ID: {}, Owner ID: {}, Is Owner?: {}",
                propertyId, currentUserId, propertyOwnerId, isOwner);
        return isOwner;
    }

    // Keep verifyOwnershipOrAdmin for potential admin use cases if needed
    private void verifyOwnershipOrAdmin(Property property) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[verifyOwnershipOrAdmin] Checking permissions for property ID: " + property.getId());
        System.out.println("[verifyOwnershipOrAdmin] Authentication object: " + authentication);

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Ownership/Admin verification failed: No authentication found.");
            throw new SecurityException("Authentication required.");
        }

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found in DB: " + currentUsername));
        Long currentUserId = currentUser.getId();

        System.out.println("[verifyOwnershipOrAdmin] Current Username (Email): " + currentUsername);
        System.out.println("[verifyOwnershipOrAdmin] Current User ID: " + currentUserId);
        System.out.println("[verifyOwnershipOrAdmin] Current Authorities: " + authentication.getAuthorities());

        Long propertyOwnerId = property.getOwnerId();
        if (propertyOwnerId == null && property.getOwner() != null) {
            propertyOwnerId = property.getOwner().getId();
        }

        System.out.println("[verifyOwnershipOrAdmin] Property Owner ID: " + propertyOwnerId);

        if (propertyOwnerId == null) {
            logger.error("Ownership/Admin verification failed: Property ID {} has null ownerId.", property.getId());
            throw new IllegalStateException("Property owner information is missing or not loaded.");
        }

        boolean isOwner = propertyOwnerId.equals(currentUserId);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        System.out.println("[verifyOwnershipOrAdmin] Is Owner (ID Check)? " + isOwner);
        System.out.println("[verifyOwnershipOrAdmin] Is Admin? " + isAdmin);

        if (!isOwner && !isAdmin) {
            logger.warn("Permission denied: User '{}' (ID:{}, Roles: {}) is not owner (ID:{}) or admin for property ID {}",
                    currentUsername, currentUserId, authentication.getAuthorities(), propertyOwnerId, property.getId());
            System.out.println("[verifyOwnershipOrAdmin] THROWING SecurityException!");
            throw new SecurityException("User does not have permission to modify this property.");
        }

        if (isOwner) logger.debug("Ownership verified for user '{}' and property ID {}", currentUsername, property.getId());
        else if (isAdmin) logger.info("Admin user '{}' granted access to property ID {}", currentUsername, property.getId());
    }

    // --- Helper Methods ---
    private Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.ofNullable(authentication.getName());
    }
}