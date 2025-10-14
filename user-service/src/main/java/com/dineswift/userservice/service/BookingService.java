package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.CartException;
import com.dineswift.userservice.model.entites.Booking;
import com.dineswift.userservice.model.entites.BookingStatus;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.model.request.BookingRequest;
import com.dineswift.userservice.model.response.PaymentCreateResponse;
import com.dineswift.userservice.repository.BookingRepository;
import com.dineswift.userservice.repository.CartRepository;
import com.dineswift.userservice.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final RestClient restClient;


    public PaymentCreateResponse bookTable(UUID cartId, BookingRequest bookingRequest) {
        log.info("Processing booking for cartId: {}", cartId);
        boolean isValidCart=cartRepository.existsByIdAndIsActive(cartId);
        if (!isValidCart)
            throw new CartException("Invalid or inactive cart ID: " + cartId);

        PaymentCreateResponse paymentCreateResponse = createPaymentResponse(cartId, bookingRequest);
        log.info("Booking processed successfully for cartId: {}", cartId);
        createBookingRecord(cartId, bookingRequest, paymentCreateResponse);
        return paymentCreateResponse;
    }

    private void createBookingRecord(UUID cartId, BookingRequest bookingRequest, PaymentCreateResponse paymentCreateResponse) {
        log.info("Creating booking record for cartId: {}", cartId);
        Booking newBooking = new Booking();
        newBooking.setTableBookingId(paymentCreateResponse.getTableBookingId());
        newBooking.setBookingDate(bookingRequest.getBookingDate());
        newBooking.setBookingStatus(BookingStatus.ORDER_CREATED);
        User bookedUser = userRepository.findById(UUID.fromString("2594390c-d0a7-4144-b784-4f98112574f7")).get();
        newBooking.setUser(bookedUser);
        bookingRepository.save(newBooking);
        log.info("Booking record created successfully with ID: {}", newBooking.getBookingId());
    }

    private PaymentCreateResponse createPaymentResponse(UUID cartId, BookingRequest bookingRequest) {
        log.info("Sending Booking Request to Restaurant Service for cartId: {}", cartId);

        PaymentCreateResponse paymentCreateResponse = restClient.post()
                .uri("/table-booking/create-order/{cartId}",cartId)
                .body(bookingRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(PaymentCreateResponse.class);
        log.info("Received PaymentCreateResponse from Restaurant Service for cartId: {}", cartId);
        return paymentCreateResponse;
    }


}
