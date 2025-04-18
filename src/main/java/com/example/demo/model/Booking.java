package com.example.demo.model;

import com.example.demo.model.enums.BookingStatus; // Import the new enum
import jakarta.persistence.*;
// If removing Lombok, add manual getters/setters/constructors
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate; // For visit date
import java.time.LocalTime; // For visit time
import java.time.LocalDateTime; // For created/updated timestamps

@Entity
@Table(name = "bookings")
@Getter // Add if using Lombok
@Setter // Add if using Lombok
@NoArgsConstructor // Add if using Lombok
@AllArgsConstructor // Add if using Lombok
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property; // The property being booked for a visit

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer; // The user (customer) requesting the visit

    @Column(nullable = false)
    private LocalDate visitDate; // Requested date for the visit

    @Column(nullable = false)
    private LocalTime visitTime; // Requested time for the visit

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status; // Status of the booking request (PENDING, CONFIRMED, CANCELLED, COMPLETED)

    @Column(columnDefinition = "TEXT")
    private String customerNotes; // Optional notes from the customer

    @Column(columnDefinition = "TEXT")
    private String ownerAgentNotes; // Optional notes from the owner or agent managing the booking

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = BookingStatus.PENDING; // Default status on creation
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Manual Getters/Setters/Constructors if not using Lombok
    /*
    public Booking() {}
    // Getters and Setters for all fields...
    */
}