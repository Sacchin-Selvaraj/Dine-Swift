package com.dineswift.userservice.model.entites;

public enum TablePaymentStatus {
    PAYMENT_FAILED,
    PAYMENT_TAMPERED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED,
    UPFRONT_PAYMENT_PENDING,
    UPFRONT_PAYMENT_COMPLETED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED
}
