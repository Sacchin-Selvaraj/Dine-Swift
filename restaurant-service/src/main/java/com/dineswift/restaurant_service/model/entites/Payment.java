package com.dineswift.restaurant_service.model.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@RequiredArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @NotBlank(message = "Payment name is required")
    @Size(min = 1, max = 100, message = "Payment name must be between 1 and 100 characters")
    @Column(name = "payment_name", nullable = false, length = 100)
    private String paymentName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have up to 10 integer digits and 2 decimal places")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus;

    @PastOrPresent(message = "Payment date must be in the past or present")
    @Column(name = "payment_date")
    private ZonedDateTime paymentDate;

    @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
    @Column(name = "transaction_id", length = 100, unique = true)
    private String transactionId;

    @NotNull(message = "Created at timestamp is required")
    @PastOrPresent(message = "Created at must be in the past or present")
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Size(max = 1000, message = "Failure reason cannot exceed 1000 characters")
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @NotNull(message = "Table booking is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "table_booking_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_table_booking")
    )
    private TableBooking tableBooking;

}