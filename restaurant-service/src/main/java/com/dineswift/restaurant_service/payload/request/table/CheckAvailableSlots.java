package com.dineswift.restaurant_service.payload.request.table;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@RequiredArgsConstructor
public class CheckAvailableSlots {

    @NotNull(message = "Reservation date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Reservation date must be today or in the future")
    private LocalDate reservationDate;

    @NotNull(message = "Reservation time cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime reservationTime;

    @NotNull(message = "Number of guests cannot be null")
    @Max(value = 10, message = "Number of guests cannot exceed 10 So contact restaurant for large group bookings")
    private Integer numberOfGuests;


    private Long durationInMinutes;


}
