package com.dineswift.restaurant_service.model;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    CREATED,
    COMPLETED,
    FAILED,
    TAMPERED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
