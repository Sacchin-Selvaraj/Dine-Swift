package com.dineswift.restaurant_service.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "table_bookings")
@Data
@RequiredArgsConstructor
public class TableBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "table_booking_id", nullable = false, updatable = false)
    private UUID tableBookingId;

    @NotNull(message = "Dine in time is required")
    @Column(name = "dine_in_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime dineInTime;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 1440 minutes (24 hours)")
    @Column(name = "duration", nullable = false)
    private Integer duration;

    @NotNull(message = "Dine out time is required")
    @Column(name = "dine_out_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime dineOutTime;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "Number of guests must be at least 1")
    @Max(value = 20, message = "Number of guests cannot exceed 20")
    @Column(name = "no_of_guest", nullable = false)
    private Integer noOfGuest;

    @NotNull(message = "Booking status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 50)
    private BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "dish_status", length = 50)
    private DishStatus dishStatus;

    @NotNull(message = "Booking date is required")
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @NotNull(message = "Grand total is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Grand total must be greater than or equal to 0")
    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @NotNull(message = "Total dish amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total dish amount must be greater than or equal to 0")
    @Column(name = "pending_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal pendingAmount;

    @NotNull(message = "Pending amount paid status is required")
    @Column(name = "is_pending_amount_paid", nullable = false)
    private Boolean isPendingAmountPaid = false;

    @NotNull(message = "Upfront amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Upfront amount must be greater than or equal to 0")
    @Column(name = "upfront_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal upfrontAmount;

    @NotNull(message = "Upfront paid status is required")
    @Column(name = "is_upfront_paid", nullable = false)
    private Boolean isUpfrontPaid = false;

    @NotNull(message = "Table reservation only status is required")
    @Column(name = "is_only_reserve_table", nullable = false)
    private Boolean isOnlyReserveTable = false;

    @DecimalMin(value = "0.0", inclusive = true, message = "Reservation fee must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Reservation fee must have up to 10 integer digits and 2 decimal places")
    @Column(name = "reservation_fee", precision = 12, scale = 2)
    private BigDecimal reservationFee;

    @NotNull(message = "Reservation fee paid status is required")
    @Column(name = "is_reservation_fee_paid", nullable = false)
    private Boolean isReservationFeePaid = false;

    @NotNull(message = "Other guest allowed status is required")
    @Column(name = "is_other_guest_allowed", nullable = false)
    private Boolean isOtherGuestAllowed = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "Created by is required")
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "last_modified_by")
    private UUID lastModifiedBy;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @Column(name = "last_modified_date")
    @UpdateTimestamp
    private ZonedDateTime lastModifiedDate;

    @Column(name = "actual_dine_in_time")
    private LocalTime actualDineInTime;

    @Column(name = "actual_dine_out_time")
    private LocalTime actualDineOutTime;

    @NotNull(message = "Table is required")
    @ManyToOne(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable restaurantTable;

    @NotNull(message = "Restaurant is required")
    @ManyToOne(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @NotNull(message = "Guest information is required")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "guest_information_id", nullable = false)
    private GuestInformation guestInformation;

}