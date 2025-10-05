package com.dineswift.restaurant_service.payload.response.table;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class AvailableSlots {

    private UUID tableId;

    private String tableNumber;

    private String tableDescription;

    private Integer totalNumberOfSeats;

    private String tableShape;

    List<AvailableTimeSlot> availableTimeSlots;

}
