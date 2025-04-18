package com.example.demo.model.enums;

public enum PropertyStatus {
    PENDING_APPROVAL, // <<<--- ADD THIS NEW STATUS
    AVAILABLE,
    PENDING,         // For offers/bookings, different from approval pending
    SOLD,
    RENTED,
    UNAVAILABLE      // E.g., withdrawn, under maintenance
}