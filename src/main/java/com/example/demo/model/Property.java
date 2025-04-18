package com.example.demo.model;

import com.example.demo.model.enums.PropertyStatus;
import com.example.demo.model.enums.PropertyType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... other fields (address, city, price, bedrooms, bathrooms, etc.) ...
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
    private User owner;

    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = true) private LocalDateTime updatedAt;

    // Field to store image filenames (comma-separated or JSON)
    @Lob
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    // --- Manual Constructors ---
    public Property() {}
    // All args constructor including imageUrls
    public Property(Long id, String address, String city, String state, String postalCode, BigDecimal price, Integer bedrooms, Integer bathrooms, BigDecimal areaSqft, String description, PropertyType type, PropertyStatus status, User owner, LocalDateTime createdAt, LocalDateTime updatedAt, String imageUrls) {
        this.id = id; this.address = address; this.city = city; this.state = state; this.postalCode = postalCode; this.price = price; this.bedrooms = bedrooms; this.bathrooms = bathrooms; this.areaSqft = areaSqft; this.description = description; this.type = type; this.status = status; this.owner = owner; this.createdAt = createdAt; this.updatedAt = updatedAt; this.imageUrls = imageUrls;
    }

    // --- Lifecycle Callbacks ---
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); if (this.status == null) { this.status = PropertyStatus.AVAILABLE; } }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // --- Manual Getters and Setters ---
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
}