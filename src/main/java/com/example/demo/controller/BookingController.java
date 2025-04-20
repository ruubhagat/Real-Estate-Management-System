package com.example.demo.controller;

import com.example.demo.dto.BookingRequestDTO;
import com.example.demo.dto.BookingResponseDTO;
import com.example.demo.dto.BookingStatusUpdateDTO;
import com.example.demo.model.Booking;
import com.example.demo.model.Property;
import com.example.demo.model.User;
import com.example.demo.service.BookingService;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashSet; // Ensure used in DTO helper
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:3001")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService; // Correct service injection

    // --- Create Booking Request ---
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createBookingRequest(@RequestBody @Valid BookingRequestDTO request) {
        logger.info("Received booking request for property ID: {}", request.getPropertyId());
        try {
            Booking bookingDetails = new Booking();
            bookingDetails.setVisitDate(request.getVisitDate());
            bookingDetails.setVisitTime(request.getVisitTime());
            bookingDetails.setCustomerNotes(request.getCustomerNotes());
            Booking createdBooking = bookingService.createBooking(request.getPropertyId(), bookingDetails);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(createdBooking));
        } catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
        catch (AccessDeniedException e) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage())); }
        catch (Exception e) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create booking request.")); }
    }

    // --- Get Bookings (Role Specific) ---
    @GetMapping("/my/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingResponseDTO>> getMyCustomerBookings() {
        logger.info("Fetching bookings for current customer.");
        try {
            // Service now returns eager-loaded entities
            List<Booking> bookings = bookingService.findMyBookingsAsCustomer();
            List<BookingResponseDTO> dtos = bookings.stream().map(this::convertToDto).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch(Exception e) {
            logger.error("Error fetching customer bookings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/my/owner")
    @PreAuthorize("hasRole('PROPERTY_OWNER')")
    public ResponseEntity<List<BookingResponseDTO>> getMyOwnerBookings() {
        logger.info("Fetching bookings for current property owner.");
        try {
            // Service now returns eager-loaded entities
            List<Booking> bookings = bookingService.findMyBookingsAsOwner();
            List<BookingResponseDTO> dtos = bookings.stream().map(this::convertToDto).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch(Exception e) {
            logger.error("Error fetching owner bookings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookingsAdmin() {
        logger.info("Fetching all bookings for admin.");
        try {
            // Service now returns eager-loaded entities
            List<Booking> bookings = bookingService.findAllBookingsAdmin();
            List<BookingResponseDTO> dtos = bookings.stream().map(this::convertToDto).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch(AccessDeniedException e) { /* ... */ return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList()); }
        catch(Exception e) { /* ... */ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList()); }
    }


    // --- Get Specific Booking ---
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        logger.info("Fetching booking details for ID: {}", id);
        try {
            // Service now returns eager-loaded entity and checks auth
            return bookingService.findBookingByIdWithAuth(id)
                    .map(this::convertToDto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (AccessDeniedException e) { /* ... */ return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage())); }
        catch (IllegalArgumentException e) { /* ... */ return ResponseEntity.notFound().build(); }
        catch (Exception e) { /* ... */ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed fetch")); }
    }


    // --- Update MAIN Booking Status ---
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestBody @Valid BookingStatusUpdateDTO statusUpdate) {
        logger.info("Received request to update status for booking ID {} to {}", id, statusUpdate.getNewStatus());
        try {
            // Service method contains logic for who can update to which status
            Booking updatedBooking = bookingService.updateBookingStatus(id, statusUpdate.getNewStatus(), statusUpdate.getNotes());
            return ResponseEntity.ok(convertToDto(updatedBooking));
        } catch (IllegalArgumentException e) { /* ... */ return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
        catch (AccessDeniedException e) { /* ... */ return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage())); }
        catch (Exception e) { /* ... */ return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Update failed.")); }
    }

    // --- REMOVED Payment Status Endpoint from here ---


    // --- Helper Method: Convert Booking Entity to BookingResponseDTO ---
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

        // Access related data (should be loaded due to JOIN FETCH in service calls)
        if (booking.getProperty() != null) {
            Property property = booking.getProperty();
            dto.setPropertyId(property.getId());
            dto.setPropertyAddress(property.getAddress());
            dto.setPropertyCity(property.getCity());
            if (property.getOwner() != null) { // Owner should be fetched too
                dto.setOwnerId(property.getOwner().getId());
                dto.setOwnerName(property.getOwner().getName());
            } else {
                dto.setOwnerId(property.getOwnerId());
                dto.setOwnerName("N/A");
            }
        }
        if (booking.getCustomer() != null) { // Customer should be fetched too
            User customer = booking.getCustomer();
            dto.setCustomerId(customer.getId());
            dto.setCustomerName(customer.getName());
        }
        return dto;
    }
    // --- End Helper Method ---
}