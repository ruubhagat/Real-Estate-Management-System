package com.example.demo.repository;

import com.example.demo.model.Booking;
import com.example.demo.model.Property;
import com.example.demo.model.User;
import com.example.demo.model.enums.BookingStatus; // Import enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find bookings made by a specific customer
    List<Booking> findByCustomer(User customer);

    // Find bookings related to a specific property
    List<Booking> findByProperty(Property property);

    // Find bookings for a specific property owned by a specific user (Property Owner view)
    List<Booking> findByPropertyOwner(User owner);

    // Find bookings by status
    List<Booking> findByStatus(BookingStatus status);

    // Example: Find potentially conflicting bookings for a property on a specific date
    List<Booking> findByPropertyAndVisitDateAndStatusIn(
            Property property,
            LocalDate visitDate,
            List<BookingStatus> statuses // e.g., find PENDING or CONFIRMED
    );

    // Find a specific booking by ID and Customer (for customer actions like cancelling)
    Optional<Booking> findByIdAndCustomer(Long id, User customer);

    // Find a specific booking by ID and Property Owner (for owner actions)
    Optional<Booking> findByIdAndPropertyOwner(Long id, User owner);

}