package com.dineswift.restaurant_service.payload.request.table;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private LocalDate reservationDate;

    @NotNull(message = "Reservation time cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime reservationTime;

    @NotNull(message = "Number of guests cannot be null")
    private Integer numberOfGuests;

    private Long durationInMinutes;


}
