package com.dineswift.userservice.model.response;

import com.dineswift.userservice.model.entites.CartStatus;
import com.dineswift.userservice.model.response.restaurant_service.OrderItemDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CartDTO {

    private UUID cartId;

    private BigDecimal grandTotal;

    private LocalDateTime cartCreatedAt;

    private LocalDateTime cartUpdatedAt;

    private CartStatus cartStatus;

    private List<OrderItemDto> orderItems;

}
