package com.example.demo.service;

import com.example.demo.model.Booking;
import com.example.demo.model.Property;
import com.example.demo.model.User;
import com.example.demo.model.enums.BookingStatus;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired private BookingRepository bookingRepository;
    @Autowired private PropertyRepository propertyRepository;
    @Autowired private UserRepository userRepository;

    /**
     * Creates a booking request for a property visit.
     * Associates the booking with the currently logged-in customer.
     * @param propertyId The ID of the property to book.
     * @param bookingRequest Booking object containing visitDate, visitTime, customerNotes.
     * @return The created Booking object.
     * @throws IllegalArgumentException if property not found or user not authenticated/found.
     */
    @Transactional
    public Booking createBooking(Long propertyId, Booking bookingRequest) {
        // 1. Get current user (customer)
        String customerEmail = getCurrentUsername()
                .orElseThrow(() -> new SecurityException("User must be logged in to create a booking."));
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + customerEmail));

        // 2. Get the property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with ID: " + propertyId));

        // 3. Populate the booking object
        Booking newBooking = new Booking();
        newBooking.setCustomer(customer);
        newBooking.setProperty(property);
        newBooking.setVisitDate(bookingRequest.getVisitDate());
        newBooking.setVisitTime(bookingRequest.getVisitTime());
        newBooking.setCustomerNotes(bookingRequest.getCustomerNotes());
        // Status (PENDING) and createdAt are set by @PrePersist

        // 4. Save the booking
        Booking savedBooking = bookingRepository.save(newBooking);
        logger.info("Booking created with ID {} for property ID {} by customer {}",
                savedBooking.getId(), propertyId, customerEmail);

        // TODO: Implement notification logic (e.g., email owner) here

        return savedBooking;
    }

    /**
     * Updates the status of an existing booking.
     * Performs authorization checks based on who is updating.
     * @param bookingId The ID of the booking to update.
     * @param newStatus The new status to set.
     * @param notes Optional notes related to the status update (e.g., reason for cancellation).
     * @return The updated Booking object.
     * @throws IllegalArgumentException if booking not found.
     * @throws SecurityException if the user lacks permission to update the status.
     */
    @Transactional
    public Booking updateBookingStatus(Long bookingId, BookingStatus newStatus, String notes) {
        // 1. Find the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        // 2. Authorization: Who can change the status?
        String currentUserEmail = getCurrentUsername()
                .orElseThrow(() -> new SecurityException("Authentication required."));
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found: " + currentUserEmail));


        boolean isAdmin = currentUser.getRole().equals("ADMIN");
        boolean isOwner = booking.getProperty().getOwner().equals(currentUser);
        boolean isCustomer = booking.getCustomer().equals(currentUser);

        // Define allowed transitions (example logic, adjust as needed)
        switch (newStatus) {
            case CONFIRMED:
            case REJECTED:
                if (!isOwner && !isAdmin) {
                    logger.warn("User {} (Customer? {}) attempted to confirm/reject booking {}", currentUserEmail, isCustomer, bookingId);
                    throw new SecurityException("Only the property owner or admin can confirm/reject bookings.");
                }
                booking.setOwnerAgentNotes(notes); // Owner/Admin notes
                break;
            case CANCELLED:
                if (!isCustomer && !isOwner && !isAdmin) { // Customer, Owner, or Admin can cancel
                    logger.warn("Unauthorized attempt to cancel booking {} by user {}", bookingId, currentUserEmail);
                    throw new SecurityException("User does not have permission to cancel this booking.");
                }
                // Add appropriate note based on who cancelled
                if (isCustomer) booking.setCustomerNotes("Cancelled by customer: " + notes);
                else booking.setOwnerAgentNotes("Cancelled by owner/admin: " + notes);
                break;
            case COMPLETED:
                if (!isOwner && !isAdmin) { // Only Owner/Admin can mark as completed
                    logger.warn("Unauthorized attempt to mark booking {} as completed by user {}", bookingId, currentUserEmail);
                    throw new SecurityException("Only the property owner or admin can mark bookings as completed.");
                }
                booking.setOwnerAgentNotes(notes);
                break;
            case PENDING:
                // Usually shouldn't revert to PENDING manually, but might be needed
                if (!isAdmin) { // Only admin can reset to pending?
                    throw new SecurityException("Only an admin can reset a booking to PENDING.");
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported status transition.");
        }

        // 3. Update status and save
        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);
        logger.info("Booking ID {} status updated to {} by user {}", bookingId, newStatus, currentUserEmail);

        // TODO: Implement notification logic (e.g., email customer/owner) here

        return updatedBooking;
    }


    // --- Retrieval Methods (with Authorization Considerations) ---

    /**
     * Finds bookings made by the currently logged-in customer.
     * @return List of bookings for the current customer.
     */
    @Transactional(readOnly = true)
    public List<Booking> findMyBookingsAsCustomer() {
        String customerEmail = getCurrentUsername()
                .orElseThrow(() -> new SecurityException("Authentication required."));
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + customerEmail));

        return bookingRepository.findByCustomer(customer);
    }

    /**
     * Finds bookings related to properties owned by the currently logged-in user (Property Owner).
     * @return List of bookings for properties owned by the current user.
     */
    @Transactional(readOnly = true)
    public List<Booking> findMyBookingsAsOwner() {
        String ownerEmail = getCurrentUsername()
                .orElseThrow(() -> new SecurityException("Authentication required."));
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found: " + ownerEmail));

        return bookingRepository.findByPropertyOwner(owner);
    }

    /**
     * Finds a specific booking by ID. Includes authorization check.
     * Allows retrieval by Customer, Property Owner, or Admin.
     * @param bookingId The ID of the booking.
     * @return Optional containing the Booking if found and authorized.
     */
    @Transactional(readOnly = true)
    public Optional<Booking> findBookingByIdWithAuth(Long bookingId) {
        String currentUserEmail = getCurrentUsername()
                .orElseThrow(() -> new SecurityException("Authentication required."));
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found: " + currentUserEmail));

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            boolean isAdmin = currentUser.getRole().equals("ADMIN");
            boolean isOwner = booking.getProperty().getOwner().equals(currentUser);
            boolean isCustomer = booking.getCustomer().equals(currentUser);

            if (isAdmin || isOwner || isCustomer) {
                return bookingOpt; // Authorized
            } else {
                logger.warn("Unauthorized attempt to access booking ID {} by user {}", bookingId, currentUserEmail);
                throw new SecurityException("User does not have permission to view this booking.");
            }
        }
        return Optional.empty(); // Not found
    }

    /**
     * Finds all bookings (ADMIN ONLY).
     * @return List of all bookings.
     * @throws SecurityException if the current user is not an ADMIN.
     */
    @Transactional(readOnly = true)
    public List<Booking> findAllBookingsAdmin() {
        String currentUserEmail = getCurrentUsername()
                .orElseThrow(() -> new SecurityException("Authentication required."));
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found: " + currentUserEmail));

        if (!currentUser.getRole().equals("ADMIN")) {
            logger.warn("Non-admin user {} attempted to access all bookings.", currentUserEmail);
            throw new SecurityException("User does not have permission to view all bookings.");
        }
        return bookingRepository.findAll();
    }


    // --- Helper Method ---
    private Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.ofNullable(authentication.getName());
    }
}