package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent; // Optional: Add spring-boot-starter-validation dependency if using
import jakarta.validation.constraints.NotNull;      // Optional: Add spring-boot-starter-validation dependency if using
import java.time.LocalDate;
import java.time.LocalTime;

// DTO without Lombok - Add manual getters/setters/constructor
public class BookingRequestDTO {

    @NotNull(message = "Property ID cannot be null") // Optional validation
    private Long propertyId;

    @NotNull(message = "Visit date cannot be null") // Optional validation
    @FutureOrPresent(message = "Visit date must be today or in the future") // Optional validation
    @JsonFormat(pattern = "yyyy-MM-dd") // Ensures correct date parsing from JSON
    private LocalDate visitDate;

    @NotNull(message = "Visit time cannot be null") // Optional validation
    @JsonFormat(pattern = "HH:mm") // Ensures correct time parsing from JSON
    private LocalTime visitTime;

    private String customerNotes;

    // --- Manual No-Argument Constructor ---
    public BookingRequestDTO() {
    }

    // --- Manual Getters and Setters ---
    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public LocalTime getVisitTime() { return visitTime; }
    public void setVisitTime(LocalTime visitTime) { this.visitTime = visitTime; }

    public String getCustomerNotes() { return customerNotes; }
    public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }
}