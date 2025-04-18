package com.example.demo.controller;
import org.springframework.security.core.context.SecurityContextHolder; // <<<--- ADD THIS IMPORT
import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.dto.BookingResponseDTO; // <<<--- Import DTO
import com.example.demo.dto.BookingStatusUpdateDTO;
import com.example.demo.model.Booking;
import com.example.demo.model.Property; // Import Property
import com.example.demo.model.User;     // Import User
import com.example.demo.service.BookingService;
import org.hibernate.LazyInitializationException; // <<<--- Import for exception handling
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections; // <<<--- Import Collections
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // <<<--- Import Collectors

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:3001")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createBookingRequest(@RequestBody BookingRequestDTO request) {
        logger.info("Received booking request for property ID: {}", request.getPropertyId());
        try {
            Booking bookingDetails = new Booking();
            bookingDetails.setVisitDate(request.getVisitDate());
            bookingDetails.setVisitTime(request.getVisitTime());
            bookingDetails.setCustomerNotes(request.getCustomerNotes());

            Booking createdBooking = bookingService.createBooking(request.getPropertyId(), bookingDetails);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(createdBooking)); // <<<--- Convert to DTO
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request during booking creation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            logger.warn("Security exception during booking creation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating booking: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create booking request."));
        }
    }

    // --- Get Bookings (Role Specific) ---

    @GetMapping("/my/customer")
    // @PreAuthorize("hasRole('CUSTOMER')") // Keep commented out for now
    public ResponseEntity<List<BookingResponseDTO>> getMyCustomerBookings() { // <<<--- Return List<DTO>
        System.out.println("[BookingController] ENTERED getMyCustomerBookings method. Auth: " + SecurityContextHolder.getContext().getAuthentication());
        logger.info("Fetching bookings for current customer.");
        try {
            List<Booking> bookings = bookingService.findMyBookingsAsCustomer();
            List<BookingResponseDTO> dtos = bookings.stream() // <<<--- Convert to DTO list
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch(Exception e) {
            logger.error("Error fetching customer bookings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList()); // Return empty list on error
        }
    }

    @GetMapping("/my/owner")
    @PreAuthorize("hasRole('PROPERTY_OWNER')")
    public ResponseEntity<List<BookingResponseDTO>> getMyOwnerBookings() { // <<<--- Return List<DTO>
        System.out.println("[BookingController] ENTERED getMyOwnerBookings method. Auth: " + SecurityContextHolder.getContext().getAuthentication());
        logger.info("Fetching bookings for current property owner.");
        try {
            List<Booking> bookings = bookingService.findMyBookingsAsOwner();
            List<BookingResponseDTO> dtos = bookings.stream() // <<<--- Convert to DTO list
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch(Exception e) {
            logger.error("Error fetching owner bookings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookingsAdmin() { // <<<--- Return List<DTO>
        System.out.println("[BookingController] ENTERED getAllBookingsAdmin method. Auth: " + SecurityContextHolder.getContext().getAuthentication());
        logger.info("Fetching all bookings for admin.");
        try {
            List<Booking> bookings = bookingService.findAllBookingsAdmin();
            List<BookingResponseDTO> dtos = bookings.stream() // <<<--- Convert to DTO list
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch(SecurityException e) {
            logger.warn("Security exception fetching all bookings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
        } catch(Exception e) {
            logger.error("Error fetching all bookings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }


    // --- Get Specific Booking ---
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) { // Return generic ResponseEntity<?>
        System.out.println("[BookingController] ENTERED getBookingById method. Auth: " + SecurityContextHolder.getContext().getAuthentication());
        logger.info("Fetching booking details for ID: {}", id);
        try {
            return bookingService.findBookingByIdWithAuth(id)
                    .map(this::convertToDto) // <<<--- Convert to DTO
                    .map(ResponseEntity::ok) // Wrap DTO in ResponseEntity
                    .orElse(ResponseEntity.notFound().build());
        } catch (SecurityException e) {
            logger.warn("Security exception fetching booking {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch booking details."));
        }
    }


    // --- Update Booking Status ---
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestBody BookingStatusUpdateDTO statusUpdate) { // Return generic ResponseEntity<?>
        System.out.println("[BookingController] ENTERED updateBookingStatus method. Auth: " + SecurityContextHolder.getContext().getAuthentication());
        logger.info("Received request to update status for booking ID {} to {}", id, statusUpdate.getNewStatus());
        try {
            Booking updatedBooking = bookingService.updateBookingStatus(
                    id, statusUpdate.getNewStatus(), statusUpdate.getNotes()
            );
            return ResponseEntity.ok(convertToDto(updatedBooking)); // <<<--- Convert to DTO
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request during booking status update {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            logger.warn("Security exception updating status for booking {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating status for booking {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update booking status."));
        }
    }

    // --- Helper Method: Convert Booking Entity to BookingResponseDTO ---
    private BookingResponseDTO convertToDto(Booking booking) {
        if (booking == null) return null;
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId()); // Assuming manual getters/setters
        dto.setStatus(booking.getStatus());
        dto.setCustomerNotes(booking.getCustomerNotes());
        dto.setOwnerAgentNotes(booking.getOwnerAgentNotes());
        dto.setVisitDate(booking.getVisitDate());
        dto.setVisitTime(booking.getVisitTime());
        dto.setCreatedAt(booking.getCreatedAt());

        // Safely access related entities, handling potential lazy loading issues
        try {
            if (booking.getProperty() != null) {
                Property property = booking.getProperty();
                dto.setPropertyId(property.getId());
                dto.setPropertyAddress(property.getAddress()); // Add more fields if needed in DTO
                dto.setPropertyCity(property.getCity());
                // Safely get owner info from Property's owner
                if (property.getOwner() != null) {
                    dto.setOwnerId(property.getOwner().getId());
                    dto.setOwnerName(property.getOwner().getName());
                }
            }
        } catch (LazyInitializationException e) {
            logger.warn("LazyInitializationException accessing property details for booking ID: {}", booking.getId());
            // Set placeholders or IDs if available without full object load
            dto.setPropertyId(booking.getProperty() != null ? booking.getProperty().getId() : null); // May still fail if proxy not initialized even for ID
            dto.setPropertyAddress("Property data not fully loaded");
            dto.setPropertyCity(null);
            dto.setOwnerId(null);
            dto.setOwnerName(null);
        }

        try {
            if (booking.getCustomer() != null) {
                User customer = booking.getCustomer();
                dto.setCustomerId(customer.getId());
                dto.setCustomerName(customer.getName());
            }
        } catch (LazyInitializationException e) {
            logger.warn("LazyInitializationException accessing customer details for booking ID: {}", booking.getId());
            dto.setCustomerId(booking.getCustomer() != null ? booking.getCustomer().getId() : null);
            dto.setCustomerName("Customer data not fully loaded");
        }

        return dto;
    }
}