package com.dineswift.userservice.service;

import com.dineswift.userservice.model.entites.BookingStatus;

import java.time.LocalDate;

public record BookingFilter(
        int page,
        int limit,
        BookingStatus bookingStatus,
        LocalDate bookingDate,
        String sortField,
        String sortOrder
) {
}
