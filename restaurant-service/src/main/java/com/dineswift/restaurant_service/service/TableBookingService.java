package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.TableBookingException;
import com.dineswift.restaurant_service.model.*;
import com.dineswift.restaurant_service.payload.request.tableBooking.BookingRequest;
import com.dineswift.restaurant_service.payload.response.tableBooking.PaymentCreateResponse;
import com.dineswift.restaurant_service.payment.service.PaymentService;
import com.dineswift.restaurant_service.repository.OrderItemRepository;
import com.dineswift.restaurant_service.repository.TableBookingRepository;
import com.dineswift.restaurant_service.repository.TableRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TableBookingService {

    private final TableBookingRepository tableBookingRepository;
    private final TableRepository tableRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;


    public PaymentCreateResponse createOrder(UUID cartId, BookingRequest bookingRequest) {

        log.info("Creating order for cartId: {}", cartId);
        UUID tableId = bookingRequest.getTableId();
        RestaurantTable bookingTable = tableRepository.findByIdAndIsActive(tableId)
                .orElseThrow(() -> new TableBookingException("Invalid table ID: " + tableId));

        if (bookingRequest.getNoOfGuest()>bookingTable.getTotalNumberOfSeats()){
            throw new TableBookingException("Number of guests exceeds table capacity.");
        }

        List<OrderItem> orderItems = getOrderItemsByCartId(cartId);

        checkSlotAvailability(bookingRequest, bookingTable);

        log.info("Slot available. Proceeding with booking for cartId: {}", cartId);
        TableBooking newBooking = bookTable(bookingRequest, bookingTable, orderItems);

        log.info("Initiating payment for booking ID: {}", newBooking.getTableBookingId());
        PaymentCreateResponse paymentResponse = paymentService.initiatePayment(newBooking);
        log.info("Payment initiated successfully for booking ID: {}", newBooking.getTableBookingId());
        return paymentResponse;
    }

    private TableBooking bookTable(BookingRequest bookingRequest, RestaurantTable bookingTable, List<OrderItem> orderItems) {
        TableBooking newBooking = new TableBooking();
        newBooking.setDineInTime(bookingRequest.getDineInTime().toLocalTime());
        newBooking.setDuration(bookingRequest.getDuration());
        newBooking.setDineOutTime(bookingRequest.getDineInTime().plusMinutes(bookingRequest.getDuration()).toLocalTime());
        newBooking.setNoOfGuest(bookingRequest.getNoOfGuest());
        newBooking.setBookingStatus(BookingStatus.UPFRONT_PAYMENT_PENDING);
        newBooking.setDishStatus(DishStatus.PENDING);
        newBooking.setBookingDate(bookingRequest.getBookingDate());

        log.info("Calculate the total amount for the order items");
        calculateTotalAmount(newBooking, orderItems);

        newBooking.setCreatedBy(UUID.randomUUID()); // Placeholder for actual user ID
        newBooking.setLastModifiedBy(UUID.randomUUID()); // Placeholder for actual user ID
        newBooking.setRestaurantTable(bookingTable);
        newBooking.setRestaurant(bookingTable.getRestaurant());
        newBooking.setIsActive(true);

        log.info("Guest Information need to be captured in future enhancements");
        GuestInformation guestInfo = getGuestInformation(bookingRequest);
        newBooking.setGuestInformation(guestInfo);

        TableBooking savedBooking = tableBookingRepository.save(newBooking);
        log.info("Table booked successfully with booking ID: {}", savedBooking.getTableBookingId());

        return savedBooking;
    }

    private GuestInformation getGuestInformation(BookingRequest bookingRequest) {
        log.info("After Authorization this method will fetch user details");
        GuestInformation guestInfo = new GuestInformation();
        if (bookingRequest.getSpecialRequest()!=null)
            guestInfo.setSpecialRequest(bookingRequest.getSpecialRequest());
        // Placeholder for actual user details
        guestInfo.setGuestName("John Doe");
        guestInfo.setContactNumber("123-456-7890");
        guestInfo.setContactEmail("sacchindemo@gmail.com");
        guestInfo.setUserId(UUID.randomUUID());
        return guestInfo;
    }

    private void calculateTotalAmount(TableBooking newBooking, List<OrderItem> orderItems) {
        BigDecimal grandTotal = orderItems.stream()
                .map(OrderItem::getFrozenTotalPrice)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        final int UPFRONT_PAYMENT_PERCENTAGE = 20;
        BigDecimal upfrontAmount = grandTotal.multiply(BigDecimal.valueOf(UPFRONT_PAYMENT_PERCENTAGE*0.01));

        BigDecimal pendingAmount = grandTotal.subtract(upfrontAmount);
        log.info("Calculated amounts - Grand Total: {}, Upfront Amount: {}, Pending Amount: {}", grandTotal, upfrontAmount, pendingAmount);
        newBooking.setGrandTotal(grandTotal);
        newBooking.setUpfrontAmount(upfrontAmount);
        newBooking.setPendingAmount(pendingAmount);
    }

    private List<OrderItem> getOrderItemsByCartId(UUID cartId) {

        log.info("Fetching order items for cartId: {}", cartId);
        List<OrderItem> orderItems = orderItemRepository.findAllByCartId(cartId);
        if (orderItems.isEmpty()) {
            throw new TableBookingException("No order items found for cart ID: " + cartId);
        }
        orderItems.forEach(OrderItem::setFrozenValues);
        return orderItems;
    }

    private void checkSlotAvailability(BookingRequest bookingRequest, RestaurantTable bookingTable) {

        List<TableBooking> existingBookings = tableBookingRepository.findByRestaurantTableAndIsActiveAndBookingDate(bookingTable,bookingRequest.getBookingDate());
        LocalTime bookingStartTime = bookingRequest.getDineInTime().toLocalTime();
        LocalTime bookingEndTime = bookingRequest.getDineInTime().plusMinutes(bookingRequest.getDuration()).toLocalTime();
        long TABLE_CLEANUP_BUFFER_MINUTES = 5L;
        for (TableBooking existingBooking : existingBookings) {
            LocalTime existingStartTime = existingBooking.getDineInTime().minusMinutes(TABLE_CLEANUP_BUFFER_MINUTES);
            LocalTime existingEndTime = existingBooking.getDineOutTime().plusMinutes(TABLE_CLEANUP_BUFFER_MINUTES);

            boolean conflict = bookingStartTime.isBefore(existingEndTime) && bookingEndTime.isAfter(existingStartTime);

            if (conflict)
                throw new TableBookingException("The selected time slot is not available. Please choose a different time.");
        }
    }
}
