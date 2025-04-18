package com.example.demo.model;

import com.example.demo.model.enums.PropertyStatus;
import com.example.demo.model.enums.PropertyType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime; // Ensure LocalDateTime is imported

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    // Assuming state can sometimes be optional based on previous discussion,
    // but if it *must* be required, keep nullable=false and ensure frontend validates/backend defaults.
    // For now, let's assume it's required as per the original entity definition.
    @Column(nullable = false, length = 50)
    private String state;

    // Same assumption for postalCode as for state.
    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer bedrooms;

    @Column(nullable = false)
    private Integer bathrooms;

    @Column(precision = 10, scale = 2)
    private BigDecimal areaSqft;

    @Lob // For potentially large text fields
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING) // Store enum names (SALE, RENT) as strings
    @Column(nullable = false, length = 20)
    private PropertyType type;

    @Enumerated(EnumType.STRING) // Store enum names (AVAILABLE, PENDING, etc.) as strings
    @Column(nullable = false, length = 20)
    private PropertyStatus status; // Now defaulted in PropertyService before save

    @Lob // For potentially long comma-separated list of filenames
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(nullable = false, updatable = false) // Cannot be null, not updated after creation
    private LocalDateTime createdAt;

    @Column(nullable = true) // Can be null initially, updated by @PreUpdate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY fetching is generally good for performance
    @JoinColumn(name = "owner_id", nullable = false) // owner_id column links to users table, cannot be null
    private User owner;

    // Direct Foreign Key Mapping (Read-only, helpful for some queries/checks)
    // Maps the owner_id column directly without loading the User object
    // insertable=false, updatable=false means JPA won't try to write this field
    @Column(name = "owner_id", insertable = false, updatable = false)
    private Long ownerId;

    // --- Constructors ---

    // JPA requires a no-argument constructor
    public Property() {}

    // Optional: Constructor for creating instances manually (less common with JPA entities)
    public Property(Long id, String address, String city, String state, String postalCode, BigDecimal price, Integer bedrooms, Integer bathrooms, BigDecimal areaSqft, String description, PropertyType type, PropertyStatus status, User owner, Long ownerId, LocalDateTime createdAt, LocalDateTime updatedAt, String imageUrls) {
        this.id = id;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.price = price;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.areaSqft = areaSqft;
        this.description = description;
        this.type = type;
        this.status = status;
        this.owner = owner;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.imageUrls = imageUrls;
    }

    // --- Lifecycle Callbacks ---

    /**
     * Automatically called by JPA before a new Property entity is persisted (saved).
     * Sets the initial creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        // --- FIX APPLIED HERE ---
        createdAt = LocalDateTime.now();
        // You could also set updatedAt here if you want it populated on creation
        // updatedAt = createdAt;
    }

    /**
     * Automatically called by JPA before an existing Property entity is updated.
     * Sets the last updated timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Manual Getters and Setters (Since Lombok was removed/problematic) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }

    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }

    public BigDecimal getAreaSqft() { return areaSqft; }
    public void setAreaSqft(BigDecimal areaSqft) { this.areaSqft = areaSqft; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PropertyType getType() { return type; }
    public void setType(PropertyType type) { this.type = type; }

    public PropertyStatus getStatus() { return status; }
    public void setStatus(PropertyStatus status) { this.status = status; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }

    // --- Getter for Owner ID ---
    public Long getOwnerId() {
        // Return the mapped ownerId field if available,
        // otherwise try getting it from the owner object (might trigger lazy load)
        if (this.ownerId != null) {
            return this.ownerId;
        } else if (this.owner != null) {
            try {
                // Accessing getId() might trigger lazy loading if 'owner' is a proxy
                return this.owner.getId(); // Use manual getter from User
            } catch (org.hibernate.LazyInitializationException e) {
                // Log or handle the exception if owner is not loaded and you need the ID
                // System.err.println("Tried to access owner ID on uninitialized proxy.");
                return null; // Or throw an exception if ID is essential here
            }
        }
        return null; // Return null if neither is available
    }
    // No setter for ownerId as it's managed by the 'owner' relationship mapping

    // --- Optional: toString, equals, hashCode ---
    // Consider overriding these if needed, especially if adding properties to Sets/Maps

} // End of class Property