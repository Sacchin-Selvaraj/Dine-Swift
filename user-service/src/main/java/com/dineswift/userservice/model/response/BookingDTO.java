package com.dineswift.userservice.model.response;

import com.dineswift.userservice.model.entites.BookingStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class BookingDTO {

    private UUID bookingId;

    private UUID tableBookingId;

    private LocalDate bookingDate;

    private BookingStatus bookingStatus;

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

}
