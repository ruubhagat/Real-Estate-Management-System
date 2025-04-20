package com.example.demo.model;

import com.example.demo.model.enums.PropertyStatus;
import com.example.demo.model.enums.PropertyType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet; // Import HashSet
import java.util.Set;     // Import Set

@Entity
@Table(name = "properties")
public class Property {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 255) private String address;
    @Column(nullable = false, length = 100) private String city;
    @Column(nullable = false, length = 50) private String state;
    @Column(nullable = false, length = 20) private String postalCode;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
    @Column(nullable = false) private Integer bedrooms;
    @Column(nullable = false) private Integer bathrooms;
    @Column(precision = 10, scale = 2) private BigDecimal areaSqft;
    @Lob @Column(columnDefinition = "TEXT") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PropertyType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PropertyStatus status;
    @Lob @Column(name = "image_urls", columnDefinition = "TEXT") private String imageUrls;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = true) private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "owner_id", insertable = false, updatable = false)
    private Long ownerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "property_amenities", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "amenity")
    private Set<String> amenities = new HashSet<>();

    // --- Constructors ---
    public Property() {}
    // Optional All-Args Constructor should also include amenities

    // --- Lifecycle Callbacks ---
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // --- Manual Getters and Setters ---

    // ... existing getters/setters for id, address, city, state, postalCode, price, etc. ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... (add all others here) ...
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
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }


    // --- VVV ADDED GETTER AND SETTER FOR AMENITIES VVV ---
    public Set<String> getAmenities() {
        // Return a defensive copy or the set itself depending on needs
        // Returning the set itself allows modification by reference if needed by JPA/Hibernate proxying
        return amenities;
        // Alternatively, for immutability outside: return new HashSet<>(amenities);
    }

    public void setAmenities(Set<String> amenities) {
        // Assign safely, handle null input
        this.amenities = (amenities != null) ? new HashSet<>(amenities) : new HashSet<>();
    }
    // --- ^^^ END ADDED GETTER AND SETTER ^^^ ---


    // --- Getter for Owner ID ---
    public Long getOwnerId() {
        if (this.ownerId != null) { return this.ownerId; }
        else if (this.owner != null) {
            try { return this.owner.getId(); }
            catch (org.hibernate.LazyInitializationException e) { return null; }
        }
        return null;
    }
    // No setter for ownerId
}