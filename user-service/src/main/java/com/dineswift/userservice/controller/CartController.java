package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.response.CartDTO;
import com.dineswift.userservice.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping("/valid-cartId/{cartId}")
    public ResponseEntity<Boolean> isValidCartId(@PathVariable UUID cartId) {
        log.info("Validating cartId={}", cartId);
        boolean response = cartService.isValidCartId(cartId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-cart/{cartId}")
    public ResponseEntity<CartDTO> getCartById(@PathVariable UUID cartId) {
        log.info("Fetching cart details for cartId={}", cartId);
        CartDTO cartDTO=cartService.getCartDetails(cartId);
        return ResponseEntity.ok(cartDTO);
    }
}
