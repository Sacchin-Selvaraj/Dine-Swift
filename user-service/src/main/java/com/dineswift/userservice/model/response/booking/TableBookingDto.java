package com.dineswift.userservice.model.response.booking;

import com.dineswift.userservice.model.entites.BookingStatus;
import com.dineswift.userservice.model.entites.DishStatus;
import com.dineswift.userservice.model.response.restaurant_service.OrderItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableBookingDto {
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
    private LocalTime actualDineInTime;
    private LocalTime actualDineOutTime;
    private List<OrderItemDto> orderItems;
}
