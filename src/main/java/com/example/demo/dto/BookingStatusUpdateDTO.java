package com.example.demo.dto;

import com.example.demo.model.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
// If removing Lombok, add manual getters/setters/constructors
import lombok.Data;


@Data // Lombok annotation
public class BookingStatusUpdateDTO {

    @NotNull(message = "New status cannot be null")
    private BookingStatus newStatus; // Expecting "PENDING", "CONFIRMED", etc.

    private String notes; // Optional notes explaining the update

    // Manual Getters/Setters if not using Lombok
    /*
    public BookingStatusUpdateDTO() {}
    // ... Getters and Setters ...
    */
}