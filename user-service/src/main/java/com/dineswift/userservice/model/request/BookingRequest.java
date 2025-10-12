package com.dineswift.userservice.model.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class BookingRequest {

    @NotNull(message = "Table ID is required")
    private UUID tableId;

    @NotNull(message = "Dine in time is required")
    @FutureOrPresent(message = "Dine in time must be in the present or future")
    private LocalTime dineInTime;

    @NotNull(message = "Duration is required")
    private Integer duration;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date must be in the present or future")
    private LocalDate bookingDate;

    @NotNull(message = "No of Guest is required")
    @Min(value = 1, message = "Minimum 1 guest is required")
    @Max(value = 15,message = "Bulk booking contact restaurant")
    private Integer noOfGuest;

    private String specialRequest;

}

