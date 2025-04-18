package com.example.demo.model.enums;

public enum BookingStatus {
    PENDING,    // Request submitted by customer, awaiting confirmation
    CONFIRMED,  // Visit confirmed by owner/agent
    CANCELLED,  // Visit cancelled by either party or admin
    COMPLETED,  // Visit took place
    REJECTED    // Visit request explicitly rejected
}