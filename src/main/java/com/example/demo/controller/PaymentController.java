package com.example.demo.controller;

import com.example.demo.dto.BookingResponseDTO; // To return updated booking info
import com.example.demo.model.Booking;
import com.example.demo.model.Property; // Needed for DTO conversion helper
import com.example.demo.model.User;     // Needed for DTO conversion helper
import com.example.demo.service.BookingService;
import org.hibernate.LazyInitializationException; // For DTO helper
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Import
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections; // For DTO helper
import java.util.HashSet;     // For DTO helper
import java.util.Map;

@RestController
@RequestMapping("/api/payments") // Base path for payment-related actions
@CrossOrigin(origins = "http://localhost:3001") // Adjust CORS as needed
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    // Inject BookingService as it handles the booking's payment status update
    // Use the specific bean name for @PreAuthorize SpEL
    @Autowired
    private BookingService bookingService;

    /**
     * Endpoint for an Owner or Admin to manually confirm that payment
     * for a specific booking has been received (e.g., offline).
     * Updates the booking's paymentStatus to "RECEIVED".
     *
     * @param bookingId The ID of the booking for which payment is confirmed.
     * @return The updated BookingResponseDTO with paymentStatus="RECEIVED".
     */
    @PostMapping("/booking/{bookingId}/confirm-manual") // Descriptive POST endpoint path
    @PreAuthorize("hasRole('ADMIN') or @bookingService.checkBookingOwnershipOrAdmin(#bookingId)") // Secure
    public ResponseEntity<?> confirmManualPaymentForBooking(@PathVariable Long bookingId) {
        logger.info("Received request to manually confirm payment for booking ID {}", bookingId);

        try {
            // Call the service method responsible for updating the payment status to RECEIVED
            Booking updatedBooking = bookingService.updatePaymentStatus(bookingId, "RECEIVED");
            // Convert the updated entity to DTO for the response
            return ResponseEntity.ok(convertToDto(updatedBooking));

        } catch (IllegalArgumentException e) { // Booking not found or invalid status update from service
            logger.warn("Bad request during manual payment confirmation for booking {}: {}", bookingId, e.getMessage());
            // Return 404 if booking not found, 400 otherwise (e.g., invalid status transition)
            if (e.getMessage() != null && e.getMessage().contains("Booking not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (AccessDeniedException e) { // Permission denied by @PreAuthorize or service check
            logger.warn("Security exception confirming payment for booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) { // Other unexpected errors
            logger.error("Error confirming manual payment for booking {}: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to confirm payment status."));
        }
    }

    // --- Optional: Endpoint to revert payment status back to PENDING ---
    /*
    @PostMapping("/booking/{bookingId}/revert-payment")
    @PreAuthorize("hasRole('ADMIN') or @bookingService.checkBookingOwnershipOrAdmin(#bookingId)")
    public ResponseEntity<?> revertPaymentConfirmation(@PathVariable Long bookingId) {
        logger.info("Received request to revert payment status for booking ID {} to PENDING", bookingId);
         try {
            Booking updatedBooking = bookingService.updatePaymentStatus(bookingId, "PENDING");
            return ResponseEntity.ok(convertToDto(updatedBooking));
        } catch (IllegalArgumentException e) { // ... error handling ... }
          catch (AccessDeniedException e) { // ... error handling ... }
          catch (Exception e) { // ... error handling ... }
    }
    */


    // --- Helper Method: Convert Booking Entity to BookingResponseDTO ---
    // Consider moving this to a shared Mapper/Util class to avoid duplication
    private BookingResponseDTO convertToDto(Booking booking) {
        if (booking == null) return null;
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setStatus(booking.getStatus());
        dto.setPaymentStatus(booking.getPaymentStatus()); // Include payment status
        dto.setCustomerNotes(booking.getCustomerNotes());
        dto.setOwnerAgentNotes(booking.getOwnerAgentNotes());
        dto.setVisitDate(booking.getVisitDate());
        dto.setVisitTime(booking.getVisitTime());
        dto.setCreatedAt(booking.getCreatedAt());
        // Safely map related data
        try {
            if (booking.getProperty() != null) {
                Property property = booking.getProperty();
                dto.setPropertyId(property.getId());
                dto.setPropertyAddress(property.getAddress());
                dto.setPropertyCity(property.getCity());
                if (property.getOwner() != null) {
                    dto.setOwnerId(property.getOwner().getId());
                    dto.setOwnerName(property.getOwner().getName());
                } else { dto.setOwnerId(property.getOwnerId()); dto.setOwnerName("N/A"); }
            }
        } catch (LazyInitializationException e) { /* ... handle lazy load exception ... */ }
        try {
            if (booking.getCustomer() != null) {
                User customer = booking.getCustomer();
                dto.setCustomerId(customer.getId());
                dto.setCustomerName(customer.getName());
            }
        } catch (LazyInitializationException e) { /* ... handle lazy load exception ... */ }
        return dto;
    }
    // --- End Helper Method ---
}