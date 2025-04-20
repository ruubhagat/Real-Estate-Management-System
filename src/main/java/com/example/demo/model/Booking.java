package com.example.demo.model;

import com.example.demo.model.enums.BookingStatus; // Ensure enum is imported
import jakarta.persistence.*;
// Remove Lombok imports if you decided against using it
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
// Remove Lombok annotations if not used
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Keep LAZY unless you *always* need property details with booking
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY) // Keep LAZY unless you *always* need customer details with booking
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false)
    private LocalTime visitTime;

    @Enumerated(EnumType.STRING) // Store status enum as String
    @Column(nullable = false, length = 30) // Added length for status
    private BookingStatus status;

    @Column(columnDefinition = "TEXT")
    private String customerNotes;

    @Column(columnDefinition = "TEXT")
    private String ownerAgentNotes;

    // --- VVV NEW PAYMENT STATUS FIELD VVV ---
    @Column(name = "payment_status", length = 30, nullable = false) // Column name explicit, NOT NULL
    private String paymentStatus; // e.g., "PENDING", "RECEIVED", "NOT_REQUIRED", "REFUNDED"
    // --- ^^^ END NEW FIELD ^^^ ---


    @Column(nullable = false, updatable = false) // Should not be updated after creation
    private LocalDateTime createdAt;

    @Column(nullable = true) // Can be null initially
    private LocalDateTime updatedAt;

    // --- Lifecycle Callbacks ---

    @PrePersist // Runs before the entity is first saved (INSERT)
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // Set creation timestamp

        // Set default booking status if not provided
        if (this.status == null) {
            this.status = BookingStatus.PENDING;
        }

        // --- VVV SET DEFAULT PAYMENT STATUS VVV ---
        // Set default payment status if not provided (should usually be PENDING for new bookings)
        if (this.paymentStatus == null) {
            this.paymentStatus = "PENDING"; // Default value
        }
        // --- ^^^ END SET DEFAULT ^^^ ---
    }

    @PreUpdate // Runs before an existing entity is updated (UPDATE)
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // Set last updated timestamp
    }


    // --- VVV ADD Manual Getters/Setters (if not using Lombok) VVV ---

    // No-argument constructor (required by JPA)
    public Booking() {}

    // Optional: All-argument constructor (remember to add paymentStatus)
    public Booking(Long id, Property property, User customer, LocalDate visitDate, LocalTime visitTime, BookingStatus status, String customerNotes, String ownerAgentNotes, String paymentStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.property = property;
        this.customer = customer;
        this.visitDate = visitDate;
        this.visitTime = visitTime;
        this.status = status;
        this.customerNotes = customerNotes;
        this.ownerAgentNotes = ownerAgentNotes;
        this.paymentStatus = paymentStatus; // Initialize paymentStatus
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // Getters and Setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public LocalTime getVisitTime() { return visitTime; }
    public void setVisitTime(LocalTime visitTime) { this.visitTime = visitTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }

    public String getOwnerAgentNotes() { return ownerAgentNotes; }
    public void setOwnerAgentNotes(String ownerAgentNotes) { this.ownerAgentNotes = ownerAgentNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // --- VVV GETTER/SETTER FOR paymentStatus VVV ---
    public String getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    // --- ^^^ END GETTER/SETTER ^^^ ---

    // --- ^^^ END Manual Getters/Setters ^^^ ---

}