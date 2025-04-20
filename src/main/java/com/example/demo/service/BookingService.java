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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

@Service("bookingService")
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired private BookingRepository bookingRepository;
    @Autowired private PropertyRepository propertyRepository;
    @Autowired private UserRepository userRepository;

    // --- Create Booking ---
    @Transactional
    public Booking createBooking(Long propertyId, Booking bookingRequest) {
        String customerEmail=getCurrentUsername().orElseThrow(()->new AccessDeniedException("Login required"));
        User customer=userRepository.findByEmail(customerEmail).orElseThrow(()->new UsernameNotFoundException("User not found"));
        Property property=propertyRepository.findById(propertyId).orElseThrow(()->new IllegalArgumentException("Property not found"));
        if(property.getOwner().equals(customer)) throw new IllegalArgumentException("Cannot book own property");
        if(property.getStatus()!=com.example.demo.model.enums.PropertyStatus.AVAILABLE) throw new IllegalArgumentException("Property not available");
        Booking newBooking = new Booking();
        newBooking.setCustomer(customer); newBooking.setProperty(property);
        newBooking.setVisitDate(bookingRequest.getVisitDate()); newBooking.setVisitTime(bookingRequest.getVisitTime());
        newBooking.setCustomerNotes(bookingRequest.getCustomerNotes());
        return bookingRepository.save(newBooking);
    }

    // --- Update MAIN Booking Status ---
    @Transactional
    public Booking updateBookingStatus(Long bookingId, BookingStatus newStatus, String notes) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(()->new IllegalArgumentException("Booking not found"));
        verifyBookingOwnershipOrAdmin(booking); // Check permission first
        switch (newStatus) {
            case CONFIRMED: case REJECTED: booking.setOwnerAgentNotes(notes); break;
            case CANCELLED: booking.setCustomerNotes("Cancelled by customer/owner/admin: " + notes); break; // Simplified note
            case COMPLETED: booking.setOwnerAgentNotes(notes); break;
            case PENDING: if(!SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"))) throw new AccessDeniedException("Admin only"); break;
            default: throw new IllegalArgumentException("Unsupported status");
        }
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    // --- Update Payment Status ---
    @Transactional
    public Booking updatePaymentStatus(Long bookingId, String newPaymentStatus) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(()->new IllegalArgumentException("Booking not found"));
        verifyBookingOwnershipOrAdmin(booking);
        if (!"RECEIVED".equalsIgnoreCase(newPaymentStatus) && !"PENDING".equalsIgnoreCase(newPaymentStatus)) throw new IllegalArgumentException("Invalid payment status");
        booking.setPaymentStatus(newPaymentStatus.toUpperCase());
        return bookingRepository.save(booking);
    }


    // --- Retrieval Methods ---
    @Transactional(readOnly = true)
    public List<Booking> findMyBookingsAsCustomer() {
        logger.debug("Fetching bookings with details for current customer");
        String customerEmail = getCurrentUsername().orElseThrow(() -> new AccessDeniedException("Auth required."));
        User customer = userRepository.findByEmail(customerEmail).orElseThrow(() -> new UsernameNotFoundException("Customer not found"));
        return bookingRepository.findByCustomerWithDetails(customer);
    }

    @Transactional(readOnly = true)
    public List<Booking> findMyBookingsAsOwner() {
        logger.debug("Fetching bookings with details for current property owner");
        String ownerEmail = getCurrentUsername().orElseThrow(() -> new AccessDeniedException("Auth required."));
        User owner = userRepository.findByEmail(ownerEmail).orElseThrow(() -> new UsernameNotFoundException("Owner not found"));
        return bookingRepository.findByPropertyOwnerWithDetails(owner);
    }

    @Transactional(readOnly = true)
    public Optional<Booking> findBookingByIdWithAuth(Long bookingId) {
        logger.debug("Fetching booking by ID {} with details and auth check", bookingId);
        Optional<Booking> bookingOpt = bookingRepository.findByIdWithDetails(bookingId); // Uses JOIN FETCH
        if (bookingOpt.isPresent()) {
            try {
                verifyBookingViewerPermissions(bookingOpt.get());
                return bookingOpt;
            } catch (AccessDeniedException e) {
                logger.warn("Unauthorized VIEW attempt for booking ID {}", bookingId);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<Booking> findAllBookingsAdmin() {
        logger.debug("Fetching all bookings with details for admin");
        String currentUserEmail = getCurrentUsername().orElseThrow(()-> new AccessDeniedException("Auth required."));
        User currentUser = userRepository.findByEmail(currentUserEmail).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!currentUser.getRole().equals("ADMIN")) { throw new AccessDeniedException("Admin role required."); }
        return bookingRepository.findAllWithDetails(); // Uses JOIN FETCH
    }


    // --- Security Helper Methods ---

    /** Checks if current user owns the booked property OR is ADMIN. Throws AccessDeniedException if not. */
    public void verifyBookingOwnershipOrAdmin(Booking booking) {
        String currentUserEmail = getCurrentUsername().orElseThrow(() -> new AccessDeniedException("Authentication required."));
        User currentUser = userRepository.findByEmail(currentUserEmail).orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUserEmail));
        boolean isAdmin = currentUser.getRole().equals("ADMIN");
        boolean isOwner = false;
        if (booking.getProperty() != null && booking.getProperty().getOwner() != null) {
            isOwner = booking.getProperty().getOwner().equals(currentUser);
        } else {
            logger.warn("Could not determine property owner for booking ID {} during auth check.", booking.getId());
            // --- VVV FIX: Provide error message string VVV ---
            throw new AccessDeniedException("Cannot verify ownership due to missing booking property/owner data.");
            // --- ^^^ END FIX ^^^ ---
        }
        if (!isAdmin && !isOwner) {
            logger.warn("Unauthorized attempt to modify booking ID {} by non-owner/non-admin user {}", booking.getId(), currentUserEmail);
            throw new AccessDeniedException("User does not have permission to modify this booking.");
        }
        logger.debug("Modify permission verified for booking ID {} by user {}", booking.getId(), currentUserEmail);
    }

    /** Checks if current user can VIEW the booking (Customer, Owner, or Admin). Throws AccessDeniedException if not. */
    private void verifyBookingViewerPermissions(Booking booking) {
        String currentUserEmail = getCurrentUsername().orElseThrow(() -> new AccessDeniedException("Authentication required."));
        User currentUser = userRepository.findByEmail(currentUserEmail).orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUserEmail));
        boolean isAdmin = currentUser.getRole().equals("ADMIN");
        boolean isOwner = false;
        boolean isCustomer = false;
        if (booking.getProperty() != null && booking.getProperty().getOwner() != null) {
            isOwner = booking.getProperty().getOwner().equals(currentUser);
        }
        if (booking.getCustomer() != null) {
            isCustomer = booking.getCustomer().equals(currentUser);
        }
        if (!isAdmin && !isOwner && !isCustomer) {
            logger.warn("Unauthorized attempt to VIEW booking ID {} by user {}", booking.getId(), currentUserEmail);
            throw new AccessDeniedException("User does not have permission to view this booking.");
        }
        logger.debug("View permission verified for booking ID {} by user {}", booking.getId(), currentUserEmail);
    }

    // Method for @PreAuthorize check by ID
    @Transactional(readOnly = true)
    public boolean checkBookingOwnershipOrAdmin(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking check failed: Booking not found with ID: " + bookingId));
        try {
            // Logging moved inside verify method for clarity if needed
            verifyBookingOwnershipOrAdmin(booking);
            logger.info("[PreAuth Check] Result for booking ID {}: true (Verification Passed)", bookingId);
            return true;
        } catch (AccessDeniedException | UsernameNotFoundException e) {
            logger.warn("[PreAuth Check] Result for booking ID {}: false (Reason: {})", bookingId, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("[PreAuth Check] Unexpected error during check for booking {}: {}", bookingId, e.getMessage(), e);
            return false;
        }
    }

    // Helper Method to get current username
    private Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.ofNullable(authentication.getName());
    }
}