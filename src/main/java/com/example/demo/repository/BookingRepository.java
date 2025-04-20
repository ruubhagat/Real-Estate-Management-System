package com.example.demo.repository;

import com.example.demo.model.Booking;
import com.example.demo.model.Property;
import com.example.demo.model.User;
import com.example.demo.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query
import org.springframework.data.repository.query.Param; // Import Param
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // --- VVV Methods using JOIN FETCH for Eager Loading VVV ---

    /** Finds bookings by customer, eagerly fetching Property and its Owner, and Customer. Ordered by creation date descending. */
    @Query("SELECT b FROM Booking b JOIN FETCH b.property p JOIN FETCH p.owner JOIN FETCH b.customer WHERE b.customer = :customer ORDER BY b.createdAt DESC")
    List<Booking> findByCustomerWithDetails(@Param("customer") User customer);

    /** Finds bookings by property owner, eagerly fetching Property and Customer. Ordered by creation date descending. */
    @Query("SELECT b FROM Booking b JOIN FETCH b.property p JOIN FETCH b.customer WHERE p.owner = :owner ORDER BY b.createdAt DESC")
    List<Booking> findByPropertyOwnerWithDetails(@Param("owner") User owner);

    /** Finds all bookings, eagerly fetching Property, its Owner, and Customer. Ordered by creation date descending. (For Admin) */
    @Query("SELECT b FROM Booking b JOIN FETCH b.property p JOIN FETCH p.owner JOIN FETCH b.customer ORDER BY b.createdAt DESC")
    List<Booking> findAllWithDetails();

    /** Finds a single booking by ID, eagerly fetching Property, its Owner, and Customer. */
    @Query("SELECT b FROM Booking b JOIN FETCH b.property p JOIN FETCH p.owner JOIN FETCH b.customer WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    // --- ^^^ END Methods using JOIN FETCH ^^^ ---


    // Original simple finders (might be used elsewhere or can be removed if WithDetails covers all cases)
    List<Booking> findByCustomer(User customer);
    List<Booking> findByPropertyOwner(User owner);
    List<Booking> findByProperty(Property property);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByPropertyAndVisitDateAndStatusIn(Property property, LocalDate visitDate, List<BookingStatus> statuses);
    Optional<Booking> findByIdAndCustomer(Long id, User customer);
    Optional<Booking> findByIdAndPropertyOwner(Long id, User owner);

}