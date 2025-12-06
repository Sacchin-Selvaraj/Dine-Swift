package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.CartException;
import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.Booking;
import com.dineswift.userservice.model.entites.BookingStatus;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.model.request.BookingRequest;
import com.dineswift.userservice.model.response.PaymentCreateResponse;
import com.dineswift.userservice.model.response.booking.TableBookingDto;
import com.dineswift.userservice.model.response.booking.TableBookingResponse;
import com.dineswift.userservice.repository.BookingRepository;
import com.dineswift.userservice.repository.CartRepository;
import com.dineswift.userservice.repository.UserRepository;
import com.dineswift.userservice.security.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @CacheEvict(value = "booking:pages", allEntries = true)
    public TableBookingResponse bookTable(UUID cartId, BookingRequest bookingRequest) {
        log.info("Processing booking for cartId: {}", cartId);
        boolean isValidCart=cartRepository.existsByIdAndIsActive(cartId);
        if (!isValidCart)
            throw new CartException("Invalid or inactive cart ID: " + cartId);

        TableBookingResponse tableBookingResponse = createBookingResponse(cartId, bookingRequest);
        log.info("Booking processed successfully for cartId: {}", cartId);
        createBookingRecord(cartId, bookingRequest, tableBookingResponse);
        return tableBookingResponse;
    }

    private void createBookingRecord(UUID cartId, BookingRequest bookingRequest, TableBookingResponse tableBookingResponse) {
        log.info("Creating booking record for cartId: {}", cartId);
        Booking newBooking = new Booking();
        newBooking.setTableBookingId(tableBookingResponse.getTableBookingId());
        newBooking.setBookingDate(bookingRequest.getBookingDate());
        newBooking.setBookingStatus(tableBookingResponse.getBookingStatus());

        log.info("Fetching authenticated user for booking record creation");
        UUID userId = authService.getAuthenticatedUserId();
        User bookedUser = userRepository.findById(userId).orElseThrow(()-> new UserException("No user found with ID: " + userId));
        newBooking.setUser(bookedUser);
        bookingRepository.save(newBooking);
        log.info("Booking record created successfully with ID: {}", newBooking.getBookingId());
    }

    private TableBookingResponse createBookingResponse(UUID cartId, BookingRequest bookingRequest) {
        log.info("Sending Booking Request to Restaurant Service for cartId: {}", cartId);

        TableBookingResponse tableBookingResponse = restClient.post()
                .uri("/table-booking/create-order/{cartId}",cartId)
                .body(bookingRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(TableBookingResponse.class);
        log.info("Received tableBookingDto from Restaurant Service for cartId: {}", cartId);
        return tableBookingResponse;
    }


    public PaymentCreateResponse generateBill(UUID bookingId) {
        log.info("Generating pay-now link for bookingId: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CartException("Booking not found with ID: " + bookingId));

        log.info("Fetching pay-now link from Restaurant Service for bookingId: {}", bookingId);
        return getPaymentCreateResponse(booking.getTableBookingId());

    }

    @CacheEvict( value = { "booking:pages", "booking:details" }, allEntries = true)
    public PaymentCreateResponse getPaymentCreateResponse(UUID tableBookingId) {
        ResponseEntity<PaymentCreateResponse> responseEntity = restClient.post()
                .uri("/payments/pay-now/{tableBookingId}", tableBookingId)
                .retrieve()
                .toEntity(PaymentCreateResponse.class);

        log.info("Pay-now link generated successfully for bookingId: {}", tableBookingId);
        return responseEntity.getBody();
    }

    @Cacheable(
            value = "booking:details",
            key = "#bookingId",
            unless = "#result == null"
    )
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

    @CacheEvict( value = { "booking:pages", "booking:details" }, allEntries = true)
    public void updateBookingStatus(UUID tableBookingId, String status) {
        log.info("Updating booking status for tableBookingId: {}", tableBookingId);
        try {
            Booking booking = bookingRepository.findByTableBookingId(tableBookingId)
                    .orElseThrow(() -> new CartException("Booking not found with Table Booking ID: " + tableBookingId));

            booking.setBookingStatus(BookingStatus.valueOf(status));
            bookingRepository.save(booking);
        } catch (IllegalArgumentException e) {
            log.error("Invalid booking status provided: {}", status);
        }
        log.info("Booking status updated successfully for tableBookingId: {}", tableBookingId);
    }
}
