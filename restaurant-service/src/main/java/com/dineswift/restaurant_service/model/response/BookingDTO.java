package com.dineswift.restaurant_service.model.response;

import com.dineswift.userservice.model.entites.BookingStatus;
import com.dineswift.userservice.model.entites.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class BookingDTO {

    private UUID bookingId;

    private UUID tableBookingId;

    private LocalDateTime bookingTime;

    private BookingStatus bookingStatus;

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

}
