package com.dineswift.restaurant_service.payload.response.tableBooking;

import com.dineswift.restaurant_service.model.BookingStatus;
import com.dineswift.restaurant_service.model.DishStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class TableBookingResponse {

    private UUID tableBookingId;
    private LocalTime dineInTime;
    private Integer duration;
    private LocalTime dineOutTime;
    private Integer noOfGuest;
    private BookingStatus bookingStatus;
    private DishStatus dishStatus;
    private LocalDate bookingDate;
    private BigDecimal grandTotal;
    private BigDecimal pendingAmount;
    private Boolean isPendingAmountPaid;
    private BigDecimal upfrontAmount;
    private Boolean isUpfrontPaid;
}
