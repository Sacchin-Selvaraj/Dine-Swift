package com.dineswift.userservice.service;

import com.dineswift.userservice.mapper.CartMapper;
import com.dineswift.userservice.model.entites.Cart;
import com.dineswift.userservice.model.request.CartAmountUpdateRequest;
import com.dineswift.userservice.model.response.CartDTO;
import com.dineswift.userservice.model.response.restaurant_service.OrderItemDto;
import com.dineswift.userservice.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final RestClient restClient;

    public boolean isValidCartId(UUID cartId) {
        log.info("Checking if cartId is valid: {}", cartId);
        boolean exists = cartRepository.existsByIdAndIsActive(cartId);
        log.info("CartId {} is valid: {}", cartId, exists);
        return exists;
    }

    public CartDTO getCartDetails(UUID cartId) {

        Cart cart = cartRepository.findByIdAndIsActive(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found or inactive"));

        CartDTO cartDto = cartMapper.toDto(cart);
        log.info("Cart details fetched successfully for cartId={}: {}", cartId, cartDto);
        List<OrderItemDto> orderItemDtos = fetchOrderItemsForCart(cartId);
        BigDecimal grandTotalFromOrderItems = orderItemDtos.stream().map(OrderItemDto::getTotalPrice)
                        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        cartDto.setGrandTotal(grandTotalFromOrderItems);
        cartDto.setOrderItems(orderItemDtos);

        cart.setGrandTotal(grandTotalFromOrderItems);
        cartRepository.save(cart);
        log.info("Cart grand total updated to {} for cartId={}", grandTotalFromOrderItems, cartId);
        return cartDto;
    }

    private List<OrderItemDto> fetchOrderItemsForCart(UUID cartId) {
        log.info("Fetching order items for cartId={}", cartId);
        return restClient.get()
                .uri("order-items/get-order-items/{cartId}",cartId)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(new ParameterizedTypeReference<List<OrderItemDto>>() {
                });
    }


    public void updateCartTotalAmount(UUID cartId, CartAmountUpdateRequest cartAmountUpdateRequest) {
        log.info("Updating cart total amount for cartId={} with request={}", cartId, cartAmountUpdateRequest);
        Cart cart = cartRepository.findByIdAndIsActive(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found or inactive"));
        if (cartAmountUpdateRequest.isRemoved())
            cart.setGrandTotal(cart.getGrandTotal().subtract(cartAmountUpdateRequest.getTotalDishPrice()));
        else
            cart.setGrandTotal(cart.getGrandTotal().add(cartAmountUpdateRequest.getTotalDishPrice()));

        cartRepository.save(cart);
        log.info("Cart total amount updated successfully for cartId={}", cartId);
    }
}
