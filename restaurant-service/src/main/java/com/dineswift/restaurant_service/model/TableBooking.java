package com.dineswift.restaurant_service.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Future(message = "Dine in time must be in the future")
    @Column(name = "dine_in_time", nullable = false)
    private ZonedDateTime dineInTime;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 1440 minutes (24 hours)")
    @Column(name = "duration", nullable = false)
    private Integer duration;

    @NotNull(message = "Dine out time is required")
    @Future(message = "Dine out time must be in the future")
    @Column(name = "dine_out_time", nullable = false)
    private ZonedDateTime dineOutTime;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "Number of guests must be at least 1")
    @Max(value = 50, message = "Number of guests cannot exceed 50")
    @Column(name = "no_of_guest", nullable = false)
    private Integer noOfGuest;

    @NotBlank(message = "Booking status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 50)
    private BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "dish_status", length = 50)
    private DishStatus dishStatus;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date must be in the future or present")
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @NotNull(message = "Grand total is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Grand total must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Grand total must have up to 10 integer digits and 2 decimal places")
    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @NotNull(message = "Total dish amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total dish amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Total dish amount must have up to 10 integer digits and 2 decimal places")
    @Column(name = "total_dish_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDishAmount;

    @NotNull(message = "Upfront amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Upfront amount must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Upfront amount must have up to 10 integer digits and 2 decimal places")
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

    @NotNull(message = "Created at timestamp is required")
    @PastOrPresent(message = "Created at must be in the past or present")
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @PastOrPresent(message = "Last modified date must be in the past or present")
    @Column(name = "last_modified_date")
    private ZonedDateTime lastModifiedDate;

    @NotNull(message = "Table is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @NotNull(message = "Restaurant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @NotNull(message = "Guest information is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_information_id", nullable = false)
    private GuestInformation guestInformation;

    @Column(name = "actual_dine_in_time")
    private ZonedDateTime actualDineInTime;

    @Column(name = "actual_dine_out_time")
    private ZonedDateTime actualDineOutTime;
}