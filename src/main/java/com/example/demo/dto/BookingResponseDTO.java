package com.example.demo.dto;

import com.example.demo.model.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

// Using manual getters/setters as Lombok seems unreliable in this project
public class BookingResponseDTO {

    private Long id;
    private BookingStatus status;
    private String customerNotes;
    private String ownerAgentNotes;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime visitTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // --- Include IDs and basic info from related entities ---
    private Long propertyId;
    private String propertyAddress; // Example denormalized data
    private String propertyCity;    // Example denormalized data

    private Long customerId;
    private String customerName;    // Example denormalized data

    private Long ownerId;           // ID of the property owner
    private String ownerName;       // Name of the property owner


    // --- Constructors ---
    public BookingResponseDTO() {
    }

    // --- Getters and Setters (Manual) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }

    public String getOwnerAgentNotes() { return ownerAgentNotes; }
    public void setOwnerAgentNotes(String ownerAgentNotes) { this.ownerAgentNotes = ownerAgentNotes; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public LocalTime getVisitTime() { return visitTime; }
    public void setVisitTime(LocalTime visitTime) { this.visitTime = visitTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }

    public String getPropertyCity() { return propertyCity; }
    public void setPropertyCity(String propertyCity) { this.propertyCity = propertyCity; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}