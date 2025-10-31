package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.CartException;
import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.Booking;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.model.request.BookingRequest;
import com.dineswift.userservice.model.response.PaymentCreateResponse;
import com.dineswift.userservice.model.response.booking.TableBookingDto;
import com.dineswift.userservice.repository.BookingRepository;
import com.dineswift.userservice.repository.CartRepository;
import com.dineswift.userservice.repository.UserRepository;
import com.dineswift.userservice.security.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
    private final AuthService authService;


    public TableBookingDto bookTable(UUID cartId, BookingRequest bookingRequest) {
        log.info("Processing booking for cartId: {}", cartId);
        boolean isValidCart=cartRepository.existsByIdAndIsActive(cartId);
        if (!isValidCart)
            throw new CartException("Invalid or inactive cart ID: " + cartId);

        TableBookingDto tableBookingDto = createBookingResponse(cartId, bookingRequest);
        log.info("Booking processed successfully for cartId: {}", cartId);
        createBookingRecord(cartId, bookingRequest, tableBookingDto);
        return tableBookingDto;
    }

    private void createBookingRecord(UUID cartId, BookingRequest bookingRequest, TableBookingDto tableBookingDto) {
        log.info("Creating booking record for cartId: {}", cartId);
        Booking newBooking = new Booking();
        newBooking.setTableBookingId(tableBookingDto.getTableBookingId());
        newBooking.setBookingDate(bookingRequest.getBookingDate());
        newBooking.setBookingStatus(tableBookingDto.getBookingStatus());

        log.info("Fetching authenticated user for booking record creation");
        UUID userId = authService.getAuthenticatedUserId();
        User bookedUser = userRepository.findById(userId).orElseThrow(()-> new UserException("No user found with ID: " + userId));
        newBooking.setUser(bookedUser);
        bookingRepository.save(newBooking);
        log.info("Booking record created successfully with ID: {}", newBooking.getBookingId());
    }

    private TableBookingDto createBookingResponse(UUID cartId, BookingRequest bookingRequest) {
        log.info("Sending Booking Request to Restaurant Service for cartId: {}", cartId);

        TableBookingDto tableBookingDto = restClient.post()
                .uri("/table-booking/create-order/{cartId}",cartId)
                .body(bookingRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(TableBookingDto.class);
        log.info("Received tableBookingDto from Restaurant Service for cartId: {}", cartId);
        return tableBookingDto;
    }


    public PaymentCreateResponse generateBill(UUID bookingId) {
        log.info("Generating pay-now link for bookingId: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CartException("Booking not found with ID: " + bookingId));

        log.info("Fetching pay-now link from Restaurant Service for bookingId: {}", bookingId);
        return getPaymentCreateResponse(booking.getTableBookingId());

    }

    public PaymentCreateResponse getPaymentCreateResponse(UUID tableBookingId) {
        ResponseEntity<PaymentCreateResponse> responseEntity = restClient.post()
                .uri("/payments/pay-now/{tableBookingId}", tableBookingId)
                .retrieve()
                .toEntity(PaymentCreateResponse.class);

        log.info("Pay-now link generated successfully for bookingId: {}", tableBookingId);
        return responseEntity.getBody();
    }

    public TableBookingDto viewTableBooking(UUID bookingId) {
        log.info("Fetching booking details for bookingId: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CartException("Booking not found with ID: " + bookingId));

        ResponseEntity<TableBookingDto> tableBookingDto = restClient.get()
                .uri("/table-booking/view-booking/{tableBookingId}", booking.getTableBookingId())
                .retrieve()
                .toEntity(TableBookingDto.class);

        log.info("Fetched booking details successfully for bookingId: {}", bookingId);
        return tableBookingDto.getBody();
    }
}
