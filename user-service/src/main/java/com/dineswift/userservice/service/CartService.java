package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.mapper.CartMapper;
import com.dineswift.userservice.model.entites.Cart;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.model.request.CartAmountUpdateRequest;
import com.dineswift.userservice.model.response.CartDTO;
import com.dineswift.userservice.model.response.CustomOrderItem;
import com.dineswift.userservice.model.response.restaurant_service.OrderItemDto;
import com.dineswift.userservice.repository.CartRepository;
import com.dineswift.userservice.repository.UserRepository;
import com.dineswift.userservice.security.service.AuthService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;
    private final RestClient restClient;
    private final AuthService authService;
    private final UserCommonService userCommonService;

    public boolean isValidCartId(UUID cartId) {
        log.info("Checking if cartId is valid: {}", cartId);
        boolean exists = cartRepository.existsByIdAndIsActive(cartId);
        log.info("CartId {} is valid: {}", cartId, exists);
        return exists;
    }

    @Cacheable(
            value = "userService:cartDto",
            key = "#userId",
            unless = "#result == null"
    )
    public CartDTO getCartDetails(UUID userId) {

        log.info("Fetching cart details for userId={}", userId);
        User loggedInUser = userCommonService.findValidUser(userId);

        Cart cart = loggedInUser.getCart();

        CartDTO cartDto = cartMapper.toDto(cart);

        log.info("Cart details fetched successfully for cartId={}: {}", cart.getCartId(), cartDto);
        List<OrderItemDto> orderItemDtos = fetchOrderItemsForCart(cart.getCartId()).getOrderItemDtos();

        log.info("Calculating Grand Total for cartId={}", cart.getCartId());
        BigDecimal grandTotalFromOrderItems = orderItemDtos.stream().map(OrderItemDto::getTotalPrice)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        cartDto.setGrandTotal(grandTotalFromOrderItems.setScale(2, RoundingMode.HALF_UP));
        cartDto.setOrderItems(orderItemDtos);

        cart.setGrandTotal(grandTotalFromOrderItems.setScale(2,RoundingMode.HALF_UP));
        cartRepository.save(cart);

        log.info("Cart grand total updated to {} for cartId={}", grandTotalFromOrderItems, cart.getCartId());
        return cartDto;
    }

    private CustomOrderItem fetchOrderItemsForCart(UUID cartId) {
        log.info("Fetching order items for cartId={}", cartId);

        return restClient.get()
                .uri("/order-items/get-order-items/{cartId}",cartId)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(CustomOrderItem.class);
    }

    @Transactional
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

    @CacheEvict(
            value = "userService:cartDto",
            key = "@authService.getAuthenticatedUserId()"
    )
    @Transactional
    public void clearCart() {
        UUID userId = authService.getAuthenticatedUserId();
       log.info("Clearing cart for userId={}", userId);

        User existingUser = userRepository.findByIdAndIsActive(userId)
                .orElseThrow(()-> new UserException("User not found or inactive"));

        Cart newCart = new Cart();
        newCart.setGrandTotal(BigDecimal.ZERO);
        existingUser.setCart(newCart);

        userRepository.save(existingUser);
        log.info("Cart cleared successfully for userId={}", userId);
    }

    public UUID getCurrentCartId() {
        UUID userId = authService.getAuthenticatedUserId();
        log.info("Fetching current cart ID for userId={}", userId);

        User loggedInUser = userCommonService.findValidUser(userId);
        UUID cartId = loggedInUser.getCart().getCartId();

        log.info("Current cart ID for userId={} is cartId={}", userId, cartId);
        return cartId;
    }
}
