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
import java.util.stream.Stream;

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
        Property savedProperty = propertyRepository.save(property);
        logger.info("Property created with ID {} for owner {}", savedProperty.getId(), ownerEmail);
        return savedProperty;
    }

    @Transactional
    public Optional<Property> updateProperty(Long id, Property updatedPropertyData) {
        logger.debug("Attempting to update general property data for ID: {}", id);
        Optional<Property> existingPropertyOpt = propertyRepository.findById(id);
        if (existingPropertyOpt.isEmpty()) {
            logger.warn("Update failed: Property not found with ID: {}", id);
            return Optional.empty();
        }
        Property existingProperty = existingPropertyOpt.get();
        verifyOwnershipOrAdmin(existingProperty);

        // Update general fields (NOT imageUrls here)
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

        Property savedProperty = propertyRepository.save(existingProperty);
        logger.info("Property general data updated successfully for ID: {}", id);
        return Optional.of(savedProperty);
    }

    @Transactional
    public Property updateImageUrls(Long propertyId, String newImageUrlsString) {
        logger.debug("Attempting to update image URLs for property ID: {}", propertyId);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> {
                    logger.warn("Update image URLs failed: Property not found with ID: {}", propertyId);
                    return new IllegalArgumentException("Property not found with ID: " + propertyId);
                });
        verifyOwnershipOrAdmin(property); // Check permission
        property.setImageUrls(newImageUrlsString); // Use manual setter
        Property savedProperty = propertyRepository.save(property);
        logger.info("Image URLs updated successfully for property ID: {}", propertyId);
        return savedProperty;
    }


    @Transactional
    public boolean deleteProperty(Long id) {
        logger.debug("Attempting to delete property with ID: {}", id);
        Optional<Property> propertyOpt = propertyRepository.findById(id);
        if (propertyOpt.isEmpty()) {
            logger.warn("Delete failed: Property not found with ID: {}", id);
            return false;
        }
        Property property = propertyOpt.get();
        verifyOwnershipOrAdmin(property);
        propertyRepository.deleteById(id);
        logger.info("Property deleted successfully for ID: {}", id);
        return true;
    }

    // --- Helper Methods ---
    private Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.ofNullable(authentication.getName());
    }

    // --- UPDATED verifyOwnershipOrAdmin using IDs ---
    private void verifyOwnershipOrAdmin(Property property) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[verifyOwnershipOrAdmin] Checking permissions for property ID: " + property.getId()); // Manual getter
        System.out.println("[verifyOwnershipOrAdmin] Authentication object: " + authentication);

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Ownership/Admin verification failed: No authentication found.");
            throw new SecurityException("Authentication required.");
        }

        String currentUsername = authentication.getName(); // This is the User's Email
        System.out.println("[verifyOwnershipOrAdmin] Current Username (Email): " + currentUsername);
        System.out.println("[verifyOwnershipOrAdmin] Current Authorities: " + authentication.getAuthorities());

        // 1. Get the currently authenticated User object to reliably get their ID
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found in DB for email: " + currentUsername));
        Long currentUserId = currentUser.getId(); // Use manual getter

        // 2. Get the Owner's ID from the Property's Owner relationship
        if (property.getOwner() == null) { // Use manual getter
            logger.error("Ownership/Admin verification failed: Property ID {} has null owner.", property.getId()); // Use manual getter
            throw new IllegalStateException("Property owner information is missing.");
        }
        Long ownerId = property.getOwner().getId(); // Use manual getters (Property->Owner->ID)
        System.out.println("[verifyOwnershipOrAdmin] Property Owner ID: " + ownerId);
        System.out.println("[verifyOwnershipOrAdmin] Current User ID: " + currentUserId);

        // 3. Compare IDs for ownership check
        boolean isOwner = ownerId != null && ownerId.equals(currentUserId); // Add null check for safety

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        System.out.println("[verifyOwnershipOrAdmin] Is Owner (ID Check)? " + isOwner);
        System.out.println("[verifyOwnershipOrAdmin] Is Admin? " + isAdmin);

        if (!isOwner && !isAdmin) {
            logger.warn("Permission denied: User '{}' (ID:{}, Roles: {}) is not owner (ID:{}) or admin for property ID {}",
                    currentUsername, currentUserId, authentication.getAuthorities(), ownerId, property.getId()); // Use manual getter
            System.out.println("[verifyOwnershipOrAdmin] THROWING SecurityException!");
            throw new SecurityException("User does not have permission to modify this property.");
        }

        if (isOwner) logger.debug("Ownership verified for user '{}' and property ID {}", currentUsername, property.getId()); // Use manual getter
        else if (isAdmin) logger.info("Admin user '{}' granted access to property ID {}", currentUsername, property.getId()); // Use manual getter
    }
    // --- End Updated Method ---

}