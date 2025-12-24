package com.dineswift.restaurant_service.records;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TableBookingFilter(
        UUID restaurantId,
        Integer pageNo,
        Integer pageSize,
        String tableNumber,
        LocalDate bookingDate,
        LocalTime dineInTime,
        Integer duration,
        Integer noOfGuest,
        String bookingStatus,
        String dishStatus,
        String sortBy,
        String sortDir
) {
}
