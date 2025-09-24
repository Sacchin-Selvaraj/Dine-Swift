package com.dineswift.restaurant_service.model.entites;

import com.dineswift.userservice.model.entites.CartStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_id", updatable = false, nullable = false)
    private UUID cartId;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "grand_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "cart_created_at", updatable = false, nullable = false)
    private LocalDateTime cartCreatedAt;

    @UpdateTimestamp
    @Column(name = "cart_updated_at", nullable = false)
    private LocalDateTime cartUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cart_status", nullable = false)
    private CartStatus cartStatus=CartStatus.ACTIVE;

    @NotNull(message = "Guest cart status is required")
    @Column(name = "is_guest_cart", nullable = false)
    private Boolean isGuestCart = false;

    @NotNull(message = "Active status is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}
