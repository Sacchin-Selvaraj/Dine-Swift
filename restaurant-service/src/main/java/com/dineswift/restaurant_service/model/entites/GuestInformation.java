package com.dineswift.restaurant_service.model.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "guest_information")
@Data
@RequiredArgsConstructor
public class GuestInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "guest_information_id", nullable = false, updatable = false)
    private UUID guestInformationId;

    @Size(max = 1000, message = "Special request cannot exceed 1000 characters")
    @Column(name = "special_request", columnDefinition = "TEXT")
    private String specialRequest;

    @NotBlank(message = "Guest name is required")
    @Size(min = 2, max = 100, message = "Guest name must be between 2 and 100 characters")
    @Column(name = "guest_name", nullable = false, length = 100)
    private String guestName;

    @NotBlank(message = "Contact number is required")
    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Email(message = "Contact email should be valid")
    @Size(max = 255, message = "Contact email cannot exceed 255 characters")
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cancellation fee must be greater than or equal to 0")
    @Digits(integer = 10, fraction = 2, message = "Cancellation fee must have up to 10 integer digits and 2 decimal places")
    @Column(name = "cancellation_fee", precision = 12, scale = 2)
    private BigDecimal cancellationFee;

    @Size(max = 1000, message = "Cancellation reason cannot exceed 1000 characters")
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @PastOrPresent(message = "Cancellation time must be in the past or present")
    @Column(name = "cancellation_time")
    private ZonedDateTime cancellationTime;

    @Column(name = "user_id")
    private UUID userId;
}