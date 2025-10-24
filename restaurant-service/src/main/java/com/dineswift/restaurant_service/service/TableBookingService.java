package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.DishException;
import com.dineswift.restaurant_service.exception.TableBookingException;
import com.dineswift.restaurant_service.mapper.OrderItemMapper;
import com.dineswift.restaurant_service.mapper.TableBookingMapper;
import com.dineswift.restaurant_service.model.*;
import com.dineswift.restaurant_service.payload.request.tableBooking.AddOrderItemRequest;
import com.dineswift.restaurant_service.payload.request.tableBooking.BookingRequest;
import com.dineswift.restaurant_service.payload.request.tableBooking.CancellationDetails;
import com.dineswift.restaurant_service.payload.request.tableBooking.QuantityUpdateRequest;
import com.dineswift.restaurant_service.payload.response.orderItem.OrderItemDto;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingDto;
import com.dineswift.restaurant_service.payment.service.PaymentService;
import com.dineswift.restaurant_service.repository.DishRepository;
import com.dineswift.restaurant_service.repository.OrderItemRepository;
import com.dineswift.restaurant_service.repository.TableBookingRepository;
import com.dineswift.restaurant_service.repository.TableRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TableBookingService {

    private final TableBookingRepository tableBookingRepository;
    private final TableRepository tableRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableBookingMapper tableBookingMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishRepository dishRepository;
    private final PaymentService paymentService;


    public TableBookingDto createOrder(UUID cartId, BookingRequest bookingRequest) {

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

        log.info("Set table booking to order items so that they are linked");
        orderItems.forEach(item -> item.setTableBooking(newBooking));

        return tableBookingMapper.toDto(newBooking,orderItems);
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

        log.info("Checking Availability for tableId: {} on date: {} at time: {}", bookingTable.getTableId(), bookingRequest.getBookingDate(), bookingRequest.getDineInTime());
        LocalTime bookingStartTime = bookingRequest.getDineInTime().toLocalTime();
        LocalTime bookingEndTime = bookingRequest.getDineInTime().plusMinutes(bookingRequest.getDuration()).toLocalTime();

        LocalTime restaurantOpeningTime = bookingTable.getRestaurant().getOpeningTime();
        LocalTime restaurantClosingTime = bookingTable.getRestaurant().getClosingTime();
        if (bookingStartTime.isBefore(restaurantOpeningTime) || bookingEndTime.isAfter(restaurantClosingTime)){
            log.error("Booking time {} - {} is outside restaurant operating hours {} - {}", bookingStartTime, bookingEndTime, restaurantOpeningTime, restaurantClosingTime);
            throw new TableBookingException("The booking time is outside the restaurant's operating hours.");
        }

        List<TableBooking> existingBookings = tableBookingRepository.findByRestaurantTableAndIsActiveAndBookingDate(bookingTable,bookingRequest.getBookingDate());

        long TABLE_CLEANUP_BUFFER_MINUTES = 5L;
        for (TableBooking existingBooking : existingBookings) {
            LocalTime existingStartTime = existingBooking.getDineInTime().minusMinutes(TABLE_CLEANUP_BUFFER_MINUTES);
            LocalTime existingEndTime = existingBooking.getDineOutTime().plusMinutes(TABLE_CLEANUP_BUFFER_MINUTES);

            boolean conflict = bookingStartTime.isBefore(existingEndTime) && bookingEndTime.isAfter(existingStartTime);

            if (conflict)
                throw new TableBookingException("The selected time slot is not available. Please choose a different time.");
        }
    }

    public String cancelBooking(UUID tableBookingId, CancellationDetails cancellationDetails) {
        log.info("Cancelling booking with ID: {}", tableBookingId);
        TableBooking existingBooking = tableBookingRepository.findByIdAndIsActive(tableBookingId)
                .orElseThrow(() -> new TableBookingException("Booking not found with ID: " + tableBookingId));
        log.info("Is booking eligible for cancellation check");
        String refundStatus = checkIsPaymentDone(existingBooking);
        existingBooking.setBookingStatus(BookingStatus.CANCELLED_BY_CUSTOMER);
        existingBooking.setDishStatus(DishStatus.CANCELLED);
        existingBooking.setIsActive(false);
        existingBooking.setLastModifiedBy(UUID.randomUUID());// Placeholder for actual user ID

        GuestInformation guestInformation = existingBooking.getGuestInformation();
        guestInformation.setCancellationFee(existingBooking.getUpfrontAmount());
        log.info("Cancellation fee set to: {}", existingBooking.getUpfrontAmount());
        if (cancellationDetails.getCancellationReason()!=null)
            guestInformation.setCancellationReason(cancellationDetails.getCancellationReason());
        else
            guestInformation.setCancellationReason("Not Specified by User");

        guestInformation.setCancellationTime(ZonedDateTime.now());

        tableBookingRepository.save(existingBooking);
        log.info("Booking cancelled successfully with ID: {}", tableBookingId);
        return "Booking cancelled successfully. " + refundStatus;
    }

    private String checkIsPaymentDone(TableBooking existingBooking) {
        LocalDateTime dineInDateTime = existingBooking.getBookingDate().atTime(existingBooking.getDineInTime());
        LocalDateTime refundDeadLine = dineInDateTime.minusHours(24);
        LocalDateTime currentDateTime = ZonedDateTime.now().toLocalDateTime();
        if (currentDateTime.isBefore(refundDeadLine)){
            if (existingBooking.getIsPendingAmountPaid()){
                paymentService.processRefund(existingBooking);
                return "Refund is being processed for booking ID: " + existingBooking.getTableBookingId();
            }else {
                log.info("No payment done yet. No refund applicable for booking ID: {}", existingBooking.getTableBookingId());
                return "No payment done yet. No refund applicable.";
            }
        }else {
            log.info("Cancellation request is on or after booking date. No refund applicable for booking ID: {}", existingBooking.getTableBookingId());
            return "Cancellation request is on or after booking date. No refund applicable.";
        }
    }


    public TableBookingDto viewBooking(UUID tableBookingId) {
        log.info("Viewing booking with ID: {}", tableBookingId);
        TableBooking existingBooking = tableBookingRepository.findByIdAndIsActive(tableBookingId)
                .orElseThrow(() -> new TableBookingException("Booking not found with ID: " + tableBookingId));

        TableBookingDto bookingDto = tableBookingMapper.toDto(existingBooking);
        log.info("Fetched booking details successfully for ID: {}", tableBookingId);
        return bookingDto;
    }

    public OrderItemDto updateOrderItem(UUID orderItemsId, QuantityUpdateRequest quantityUpdateRequest) {
        log.info("Updating order item with ID: {}", orderItemsId);
        OrderItem existingItem = orderItemRepository.findById(orderItemsId)
                .orElseThrow(() -> new TableBookingException("Order item not found with ID: " + orderItemsId));

        if (existingItem.getTableBooking().getDishStatus().equals(DishStatus.PREPARING) ||
                existingItem.getTableBooking().getDishStatus().equals(DishStatus.PREPARED)) {
            throw new TableBookingException("Cannot update item as the dish is already being prepared or preparing.");
        }

       int newQuantity = quantityUpdateRequest.getNewQuantity() - existingItem.getQuantity();
       if (newQuantity<0) {
            BigDecimal priceChange = existingItem.getFrozenPrice().multiply(BigDecimal.valueOf(Math.abs(newQuantity)));
            BigDecimal newTotalPrice = existingItem.getFrozenTotalPrice().subtract(priceChange);
            TableBooking tableBooking = existingItem.getTableBooking();
            tableBooking.setGrandTotal(tableBooking.getGrandTotal().subtract(priceChange));
            tableBooking.setPendingAmount(tableBooking.getPendingAmount().subtract(priceChange));
            existingItem.setFrozenTotalPrice(newTotalPrice);
       }else {
           BigDecimal additionalTotalPrice = existingItem.getFrozenPrice().multiply(BigDecimal.valueOf(newQuantity));
           BigDecimal newTotalPrice = existingItem.getFrozenTotalPrice().add(additionalTotalPrice);
           TableBooking tableBooking = existingItem.getTableBooking();
           tableBooking.setGrandTotal(tableBooking.getGrandTotal().add(additionalTotalPrice));
           tableBooking.setPendingAmount(tableBooking.getPendingAmount().add(additionalTotalPrice));
           existingItem.setFrozenTotalPrice(newTotalPrice);
       }

       log.info("Updating quantity from {} to {}", existingItem.getQuantity(), quantityUpdateRequest.getNewQuantity());
       existingItem.setQuantity(quantityUpdateRequest.getNewQuantity());
       orderItemRepository.save(existingItem);
       return orderItemMapper.toDtoAfterBooking(existingItem);
    }

    public void removeOrderItem(UUID orderItemsId) {
        log.info("Removing order item with ID: {}", orderItemsId);
        OrderItem existingItem = orderItemRepository.findById(orderItemsId)
                .orElseThrow(() -> new TableBookingException("Order item not found with ID: " + orderItemsId));

        if (existingItem.getTableBooking().getDishStatus().equals(DishStatus.PREPARING) ||
                existingItem.getTableBooking().getDishStatus().equals(DishStatus.PREPARED)) {
            throw new TableBookingException("Cannot remove item as the dish is already being prepared or preparing.");
        }

        TableBooking tableBooking = existingItem.getTableBooking();
        tableBooking.setGrandTotal(tableBooking.getGrandTotal().subtract(existingItem.getFrozenTotalPrice()));
        tableBooking.setPendingAmount(tableBooking.getPendingAmount().subtract(existingItem.getFrozenTotalPrice()));

        tableBookingRepository.save(tableBooking);
        orderItemRepository.delete(existingItem);
        log.info("Order item removed successfully with ID: {}", orderItemsId);
    }

    public OrderItemDto addOrderItem(UUID tableBookingId, AddOrderItemRequest addOrderItemRequest) {
        log.info("Adding order item to booking with ID: {}", tableBookingId);
        TableBooking existingBooking = tableBookingRepository.findByIdAndIsActive(tableBookingId)
                .orElseThrow(() -> new TableBookingException("Booking not found with ID: " + tableBookingId));

        if (existingBooking.getDishStatus().equals(DishStatus.PREPARING) ||
                existingBooking.getDishStatus().equals(DishStatus.PREPARED)) {
            throw new TableBookingException("Cannot add item as the dish is already being prepared or preparing. you can order from the restaurant separately.");
        }
        Dish dish = dishRepository.findByIdAndIsActive(addOrderItemRequest.getDishId()).orElseThrow(()->new DishException("Dish not found with ID: "+addOrderItemRequest.getDishId()));

        OrderItem newOrderItem = createNewOrderItem(existingBooking, dish, addOrderItemRequest.getQuantity());

        existingBooking.setGrandTotal(existingBooking.getGrandTotal().add(newOrderItem.getFrozenTotalPrice()));
        existingBooking.setPendingAmount(existingBooking.getPendingAmount().add(newOrderItem.getFrozenTotalPrice()));

        OrderItem savedItem = orderItemRepository.save(newOrderItem);
        log.info("Order item added successfully to booking ID: {}", tableBookingId);
        return orderItemMapper.toDtoAfterBooking(savedItem);
    }

    private OrderItem createNewOrderItem(TableBooking existingBooking, Dish dish, Integer quantity) {
        log.info("Creating new order item for dish ID: {} with quantity: {}", dish.getDishId(), quantity);
        OrderItem newOrderItem = new OrderItem();
        newOrderItem.setDish(dish);
        newOrderItem.setQuantity(quantity);
        newOrderItem.setBooked(true);
        newOrderItem.setRestaurant(existingBooking.getRestaurant());
        newOrderItem.setTableBooking(existingBooking);
        newOrderItem.setFrozenValues();
        return newOrderItem;
    }
}
