package com.orderflow.model;

public enum OrderStatus {
    PENDING,      // Just created, awaiting confirmation
    CONFIRMED,    // Confirmed, inventory reserved
    PROCESSING,   // Being prepared/packed
    SHIPPED,      // In transit
    DELIVERED,    // Successfully delivered
    CANCELLED     // Terminal state — cancelled
}