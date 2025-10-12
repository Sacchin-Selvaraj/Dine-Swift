package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.CartException;
import com.dineswift.userservice.model.request.BookingRequest;
import com.dineswift.userservice.model.response.PaymentCreateResponse;
import com.dineswift.userservice.repository.BookingRepository;
import com.dineswift.userservice.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.smartcardio.CardException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CartRepository cartRepository;
    private final RestClient restClient;

    public PaymentCreateResponse bookTable(UUID cartId, BookingRequest bookingRequest) {
        log.info("Processing booking for cartId: {}", cartId);
        boolean isValidCart=cartRepository.existsByIdAndIsActive(cartId);
        if (!isValidCart)
            throw new CartException("Invalid or inactive cart ID: " + cartId);

        return createPaymentResponse(cartId, bookingRequest);
    }

    private PaymentCreateResponse createPaymentResponse(UUID cartId, BookingRequest bookingRequest) {
        log.info("Sending Booking Request to Restaurant Service for cartId: {}", cartId);

        PaymentCreateResponse paymentCreateResponse = restClient.post()
                .uri("",cartId)
                .body(bookingRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(PaymentCreateResponse.class);
        log.info("Received PaymentCreateResponse from Restaurant Service for cartId: {}", cartId);
        return paymentCreateResponse;
    }


}
