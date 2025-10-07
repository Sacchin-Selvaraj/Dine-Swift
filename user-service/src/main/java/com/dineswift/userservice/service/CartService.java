package com.dineswift.userservice.service;

import com.dineswift.userservice.model.response.CartDTO;
import com.dineswift.userservice.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;

    public boolean isValidCartId(UUID cartId) {
        log.info("Checking if cartId is valid: {}", cartId);
        boolean exists = cartRepository.existsByIdAndIsActive(cartId);
        log.info("CartId {} is valid: {}", cartId, exists);
        return exists;
    }

    public CartDTO getCartDetails(UUID cartId) {
        return null;
    }
}
