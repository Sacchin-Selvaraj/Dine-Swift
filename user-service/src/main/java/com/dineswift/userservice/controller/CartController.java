package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.request.CartAmountUpdateRequest;
import com.dineswift.userservice.model.response.CartDTO;
import com.dineswift.userservice.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping("/valid-cartId/{cartId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Boolean> isValidCartId(@PathVariable UUID cartId) {
        log.info("Received Request from the Restaurant Service to validate cartId: {}", cartId);
        boolean response = cartService.isValidCartId(cartId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-cart-details")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CartDTO> getCartById() {
        log.info("Fetching cart details for cartId");
        CartDTO cartDTO=cartService.getCartDetails();
        return ResponseEntity.ok(cartDTO);
    }

    @PatchMapping("/update-cart-amount/{cartId}" )
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> updateCartTotalAmount(@PathVariable UUID cartId,
                                                     @RequestBody CartAmountUpdateRequest cartAmountUpdateRequest) {
        log.info("Request Received from the Restaurant Service to update cart total amount for cartId: {}", cartId);
        cartService.updateCartTotalAmount(cartId, cartAmountUpdateRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/clear-cart")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> clearCart() {
        log.info("Request Received from the User Service to clear cart");
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
