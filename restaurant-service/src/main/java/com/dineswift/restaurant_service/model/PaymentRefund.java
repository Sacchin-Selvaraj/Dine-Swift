package com.dineswift.restaurant_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "payment_refund")
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID refundId;

    @NotBlank(message = "Razorpay Refund ID is required")
    @Size(max = 50, message = "Razorpay Refund ID cannot exceed 50 characters")
    @Column(name = "razorpay_refund_id", length = 50, unique = true, nullable = false)
    private String razorpayRefundId;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than zero")
    @Column(name = "refund_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal refundAmount;

    @NotBlank(message = "Refund status is required")
    @Size(max = 20, message = "Refund status cannot exceed 20 characters")
    @Column(name = "refund_status", length = 20, nullable = false)
    private String refundStatus;

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    @Column(name = "reason", length = 255, nullable = false)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @Column(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @Column(name = "table_booking_id", nullable = false)
    private TableBooking tableBooking;

}