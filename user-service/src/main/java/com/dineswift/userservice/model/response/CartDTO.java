package com.dineswift.userservice.model.response;

import com.dineswift.userservice.model.entites.CartStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CartDTO {

    private UUID cartId;

    private BigDecimal grandTotal;

    private LocalDateTime cartCreatedAt;

    private LocalDateTime cartUpdatedAt;

    private CartStatus cartStatus;

    private Boolean isGuestCart;

    private Boolean isActive;

}
