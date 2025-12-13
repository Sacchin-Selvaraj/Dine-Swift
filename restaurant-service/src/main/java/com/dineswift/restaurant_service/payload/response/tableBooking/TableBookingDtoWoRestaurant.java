package com.dineswift.restaurant_service.payload.response.tableBooking;

import com.dineswift.restaurant_service.model.BookingStatus;
import com.dineswift.restaurant_service.model.DishStatus;
import com.dineswift.restaurant_service.model.TablePaymentStatus;
import com.dineswift.restaurant_service.payload.response.table.RestaurantTableDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableBookingDtoWoRestaurant {
    private UUID tableBookingId;
    private LocalTime dineInTime;
    private Integer duration;
    private LocalTime dineOutTime;
    private Integer noOfGuest;
    private BookingStatus bookingStatus;
    private DishStatus dishStatus;
    private TablePaymentStatus tablePaymentStatus;
    private LocalDate bookingDate;
    private BigDecimal grandTotal;
    private BigDecimal pendingAmount;
    private Boolean isPendingAmountPaid;
    private BigDecimal upfrontAmount;
    private Boolean isUpfrontPaid;
    private LocalTime actualDineInTime;
    private LocalTime actualDineOutTime;
    private RestaurantTableDto restaurantTableDto;
    private GuestInformationDto guestInformationDto;
}
