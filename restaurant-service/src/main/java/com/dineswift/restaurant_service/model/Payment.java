package com.dineswift.restaurant_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus;

    @PastOrPresent(message = "Payment date must be in the past or present")
    @Column(name = "payment_date")
    @CreationTimestamp
    private ZonedDateTime paymentDate;

    @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
    @Column(name = "transaction_id", length = 100, unique = true)
    private String transactionId;

    @NotNull(message = "Order ID is required")
    @Column(name = "order_id", length = 100, unique = true)
    private String orderId;

    @PastOrPresent(message = "Created at must be in the past or present")
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @Size(max = 1000, message = "Failure reason cannot exceed 1000 characters")
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @NotNull(message = "Table booking is required")
    @ManyToOne(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(
            name = "table_booking_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_table_booking")
    )
    private TableBooking tableBooking;

}