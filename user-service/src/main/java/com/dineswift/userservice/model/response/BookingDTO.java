package com.dineswift.userservice.model.response;

import com.dineswift.userservice.model.entites.BookingStatus;
import com.dineswift.userservice.model.entites.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


public class BookingDTO {

    private UUID bookingId;

    private UUID tableBookingId;

    private LocalDateTime bookingTime;

    private BookingStatus bookingStatus;

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

}
