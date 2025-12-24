package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.*;
import com.dineswift.restaurant_service.kafka.service.KafkaService;
import com.dineswift.restaurant_service.mapper.TableBookingMapper;
import com.dineswift.restaurant_service.model.*;
import com.dineswift.restaurant_service.payload.request.tableBooking.*;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingDto;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingDtoWoRestaurant;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingResponse;
import com.dineswift.restaurant_service.payment.service.PaymentService;
import com.dineswift.restaurant_service.repository.*;
import com.dineswift.restaurant_service.security.service.AuthService;
import com.dineswift.restaurant_service.records.TableBookingFilter;
import com.dineswift.restaurant_service.specification.TableBookingSpecification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TableBookingService {

    private final TableBookingRepository tableBookingRepository;
    private final TableRepository tableRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableBookingMapper tableBookingMapper;
    private final DishRepository dishRepository;
    private final AuthService authService;
    private final PaymentService paymentService;
    private final TableBookingSpecification tableBookingSpecification;
    private final KafkaService kafkaService;
    private final RestClient restClient;
    private final CacheManager cacheManager;


    @Caching(
            evict = {
                    @CacheEvict(
                            value = "restaurant:tableBookings",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "restaurant:cart-id-from-user-service",
                            key = "@authService.getAuthenticatedId()"
                    )
            }
    )
    @Transactional
    public TableBookingResponse createOrder(UUID cartId, BookingRequest bookingRequest) {

        log.info("Creating order for cartId: {}", cartId);
        UUID tableId = bookingRequest.getTableId();

        RestaurantTable bookingTable = tableRepository.findByIdAndIsActiveWithRestaurant(tableId)
                .orElseThrow(() -> new TableBookingException("Invalid table ID: " + tableId));

        checkSlotAvailability(bookingRequest, bookingTable);

        List<OrderItem> orderItems = getOrderItemsByCartId(cartId);

        checkOrderItemsBelongToRestaurant(orderItems, bookingTable.getRestaurant());

        log.info("Slot available. Proceeding with booking for cartId: {}", cartId);
        TableBooking newBooking = bookTable(bookingRequest, bookingTable, orderItems);

        log.info("Set table booking to order items so that they are linked");
        orderItems.forEach(item -> item.setTableBooking(newBooking));

        log.info("All checks passed. Proceeding to remove cart and create booking.");
        sendCartRemovalRequest();

        return tableBookingMapper.toBookingResponse(newBooking);
    }

    private void checkOrderItemsBelongToRestaurant(List<OrderItem> orderItems, Restaurant restaurant) {

        List<UUID> orderItemIds = orderItems.stream().map(OrderItem::getOrderItemsId).toList();

        Set<UUID> restaurantIds = orderItemRepository.findDistinctRestaurantIdsByOrderItemIds(orderItemIds);

        log.info("Checking if all order items belong to the restaurant ID: {}", restaurant.getRestaurantId());
        boolean isInvalid = restaurantIds.stream()
                .anyMatch(restaurantId-> !restaurantId.equals(restaurant.getRestaurantId()));

        if (isInvalid){
            log.error("Some order items belongs to some other Restaurant");
            throw new OrderItemException("Some order items do not belong to the selected restaurant.");
        }
    }

    private void sendCartRemovalRequest() {
        log.info("Calling cart service to remove cart");
        ResponseEntity<Void> response = restClient.put()
                .uri("/cart/clear-cart")
                .retrieve()
                .toBodilessEntity();
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Cart removal successful for cart");
        } else {
            log.error("Failed to remove cart for cart");
            throw new BookingException("Failed to remove cart");
        }
    }

    private TableBooking bookTable(BookingRequest bookingRequest,
                                   RestaurantTable bookingTable, List<OrderItem> orderItems) {

        TableBooking newBooking = new TableBooking();
        newBooking.setDineInTime(bookingRequest.getDineInTime());
        newBooking.setDuration(bookingRequest.getDuration());
        newBooking.setDineOutTime(bookingRequest.getDineInTime().plusMinutes(bookingRequest.getDuration()));
        newBooking.setNoOfGuest(bookingRequest.getNoOfGuest());
        newBooking.setBookingStatus(BookingStatus.WAITING_LIST);
        newBooking.setDishStatus(DishStatus.PENDING);
        newBooking.setTablePaymentStatus(TablePaymentStatus.UPFRONT_PAYMENT_PENDING);
        newBooking.setBookingDate(bookingRequest.getBookingDate());

        log.info("Calculate the total amount for the order items");
        calculateTotalAmount(newBooking, orderItems);

        newBooking.setCreatedBy(authService.getAuthenticatedId());
        newBooking.setLastModifiedBy(authService.getAuthenticatedId());
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

        guestInfo.setUserId(authService.getAuthenticatedId());

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
        List<OrderItem> orderItems = orderItemRepository.findAllByCartIdWithDish(cartId);

        if (orderItems.isEmpty()) {
            throw new TableBookingException("No order items found for cart ID: " + cartId);
        }
        orderItems.forEach(OrderItem::setFrozenValues);

        return orderItems;
    }

    private void checkSlotAvailability(BookingRequest bookingRequest, RestaurantTable bookingTable) {


        if (bookingRequest.getNoOfGuest()>bookingTable.getTotalNumberOfSeats()){
            log.error("Number of guests {} exceeds table capacity {}", bookingRequest.getNoOfGuest(), bookingTable.getTotalNumberOfSeats());
            throw new TableBookingException("Number of guests exceeds table capacity.");
        }

        log.info("Checking Availability for tableId: {} on date: {} at time: {}", bookingTable.getTableId(), bookingRequest.getBookingDate(), bookingRequest.getDineInTime());
        LocalTime bookingStartTime = bookingRequest.getDineInTime();
        LocalTime bookingEndTime = bookingRequest.getDineInTime().plusMinutes(bookingRequest.getDuration());

        LocalTime restaurantOpeningTime = bookingTable.getRestaurant().getOpeningTime();
        LocalTime restaurantClosingTime = bookingTable.getRestaurant().getClosingTime();

        if (bookingStartTime.isBefore(restaurantOpeningTime) || bookingEndTime.isAfter(restaurantClosingTime)){
            log.error("Booking time {} - {} is outside restaurant operating hours {} - {}", bookingStartTime, bookingEndTime, restaurantOpeningTime, restaurantClosingTime);
            throw new TableBookingException("The booking time is outside the restaurant's operating hours.");
        }

        List<TableBooking> existingBookings = tableBookingRepository
                .findByRestaurantTableAndIsActiveAndBookingDate(bookingTable,bookingRequest.getBookingDate());

        long TABLE_CLEANUP_BUFFER_MINUTES = 5L;

        for (TableBooking existingBooking : existingBookings) {
            LocalTime existingStartTime = existingBooking.getDineInTime().minusMinutes(TABLE_CLEANUP_BUFFER_MINUTES);
            LocalTime existingEndTime = existingBooking.getDineOutTime().plusMinutes(TABLE_CLEANUP_BUFFER_MINUTES);

            boolean conflict = bookingStartTime.isBefore(existingEndTime) && bookingEndTime.isAfter(existingStartTime);

            if (conflict)
                throw new TableBookingException("The selected time slot is not available. Please choose a different time.");
        }
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = "restaurant:tableBookingDetails",
                            key = "#tableBookingId"
                    ),
                    @CacheEvict(
                            value = "restaurant:tableBookings",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "booking:pages",
                            allEntries = true
                    )
            }
    )
    @Transactional
    public String cancelBooking(UUID tableBookingId, CancellationDetails cancellationDetails) {

        log.info("Cancelling booking with ID: {}", tableBookingId);
        TableBooking existingBooking = tableBookingRepository.findByIdWithGuestInformation(tableBookingId)
                .orElseThrow(() -> new TableBookingException("Booking not found with ID: " + tableBookingId));

        if (!existingBooking.getIsActive()){
            throw new TableBookingException("Booking is already cancelled with ID: " + tableBookingId);
        }

        log.info("Setting booking status to CANCELLED based on Role and User");
        Collection<? extends GrantedAuthority> authorityDefaults = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        boolean isAdminOrManager = authorityDefaults.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER"));

        if (isAdminOrManager) {
            existingBooking.setBookingStatus(BookingStatus.CANCELLED_BY_RESTAURANT);
        }
        else if(existingBooking.getGuestInformation().getUserId().equals(authService.getAuthenticatedId())) {
            existingBooking.setBookingStatus(BookingStatus.CANCELLED_BY_CUSTOMER);
        }

        existingBooking.setDishStatus(DishStatus.CANCELLED);
        existingBooking.setIsActive(false);
        existingBooking.setLastModifiedBy(authService.getAuthenticatedId());

        log.info("Is booking eligible for cancellation check");
        String refundStatus = checkIsPaymentDone(existingBooking);

        GuestInformation guestInformation = existingBooking.getGuestInformation();
        guestInformation.setCancellationFee(existingBooking.getUpfrontAmount());
        log.info("Cancellation fee set to: {}", existingBooking.getUpfrontAmount());

        if (cancellationDetails.getCancellationReason()!=null)
            guestInformation.setCancellationReason(cancellationDetails.getCancellationReason());
        else
            guestInformation.setCancellationReason("Not Specified by User");

        guestInformation.setCancellationTime(ZonedDateTime.now());

        sendNotificationViaKafka(guestInformation.getUserId(), existingBooking.getBookingStatus().name(), existingBooking,true);

        tableBookingRepository.save(existingBooking);
        log.info("Booking cancelled successfully with ID: {}", tableBookingId);
        return "Booking cancelled successfully. " + refundStatus;
    }

    private String checkIsPaymentDone(TableBooking existingBooking) {

        LocalDateTime dineInDateTime = existingBooking.getBookingDate().atTime(existingBooking.getDineInTime());
        LocalDateTime refundDeadLine = dineInDateTime.minusHours(24);
        LocalDateTime currentDateTime = ZonedDateTime.now().toLocalDateTime();

        if (currentDateTime.isBefore(refundDeadLine)){
            if (existingBooking.getIsUpfrontPaid()){
                paymentService.processRefund(existingBooking);
                existingBooking.setTablePaymentStatus(TablePaymentStatus.REFUNDED);
                return "Refund is being processed for booking ID: " + existingBooking.getTableBookingId();
            }else {
                existingBooking.setTablePaymentStatus(TablePaymentStatus.NO_REFUND);
                log.info("No payment done yet. No refund applicable for booking ID: {}", existingBooking.getTableBookingId());
                return "No payment done yet. No refund applicable.";
            }
        }else {
            existingBooking.setTablePaymentStatus(TablePaymentStatus.NO_REFUND);
            log.info("Cancellation request is on or after booking date. No refund applicable for booking ID: {}", existingBooking.getTableBookingId());
            return "Cancellation request is on or after booking date. No refund applicable.";
        }
    }

    @Cacheable(
            value = "restaurant:tableBookingDetails",
            key = "#tableBookingId",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public TableBookingDto viewBooking(UUID tableBookingId) {
        log.info("Viewing booking with ID: {}", tableBookingId);

        TableBooking existingBooking = tableBookingRepository.findByIdWithChildClass(tableBookingId)
                .orElseThrow(() -> new TableBookingException("Booking not found with ID: " + tableBookingId));

        TableBookingDto bookingDto = tableBookingMapper.toDto(existingBooking);
        log.info("Fetched booking details successfully for ID: {}", tableBookingId);
        return bookingDto;
    }

    @CacheEvict(
            value = "restaurant:order-items-by-booking",
            allEntries = true
    )
    @Transactional
    public void updateOrderItem(UUID orderItemsId, QuantityUpdateRequest quantityUpdateRequest) {
        log.info("Updating order item with ID: {}", orderItemsId);

        OrderItem existingItem = orderItemRepository.findByIdWithDishAndBooking(orderItemsId)
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

       evictCachesForTableBookingId(existingItem.getTableBooking().getTableBookingId());
       log.info("Order item updated successfully with ID: {}", orderItemsId);
    }

    @CacheEvict(
            value = "restaurant:order-items-by-booking",
            allEntries = true
    )
    @Transactional
    public void removeOrderItem(UUID orderItemsId) {
        log.info("Removing order item with ID: {}", orderItemsId);

        OrderItem existingItem = orderItemRepository.findByIdWithDishAndBooking(orderItemsId)
                .orElseThrow(() -> new TableBookingException("Order item not found with ID: " + orderItemsId));

        if (existingItem.getTableBooking().getDishStatus().equals(DishStatus.PREPARING) ||
                existingItem.getTableBooking().getDishStatus().equals(DishStatus.PREPARED)) {
            throw new TableBookingException("Cannot remove item as the dish is already being prepared or preparing.");
        }

        TableBooking tableBooking = existingItem.getTableBooking();

        tableBooking.setGrandTotal(tableBooking.getGrandTotal().subtract(existingItem.getFrozenTotalPrice()));
        tableBooking.setPendingAmount(tableBooking.getPendingAmount().subtract(existingItem.getFrozenTotalPrice()));
        tableBooking.setLastModifiedBy(authService.getAuthenticatedId());

        tableBookingRepository.save(tableBooking);
        orderItemRepository.delete(existingItem);

        evictCachesForTableBookingId(existingItem.getTableBooking().getTableBookingId());
        log.info("Order item removed successfully with ID: {}", orderItemsId);
    }

    @CacheEvict(
            value = "restaurant:order-items-by-booking",
            allEntries = true
    )
    @Transactional
    public void addOrderItem(UUID tableBookingId, AddOrderItemRequest addOrderItemRequest) {
        log.info("Adding order item to booking with ID: {}", tableBookingId);

        TableBooking existingBooking = tableBookingRepository.findByIdWithRestaurant(tableBookingId)
                .orElseThrow(() -> new TableBookingException("Booking not found with ID: " + tableBookingId));


        if (existingBooking.getDishStatus().equals(DishStatus.PREPARING) ||
                existingBooking.getDishStatus().equals(DishStatus.PREPARED)) {
            log.error("Cannot add item as the dish is already being prepared or preparing for booking ID: {}", tableBookingId);
            throw new TableBookingException("Cannot add item as the dish is already being prepared or preparing. you can order from the restaurant separately.");
        }
        Dish dish = dishRepository.findByIdAndIsActive(addOrderItemRequest.getDishId())
                .orElseThrow(()->new DishException("Dish not found with ID: "+addOrderItemRequest.getDishId()));

        OrderItem newOrderItem = createNewOrderItem(existingBooking, dish, addOrderItemRequest.getQuantity());

        existingBooking.setGrandTotal(existingBooking.getGrandTotal().add(newOrderItem.getFrozenTotalPrice()));
        existingBooking.setPendingAmount(existingBooking.getPendingAmount().add(newOrderItem.getFrozenTotalPrice()));
        existingBooking.setLastModifiedBy(authService.getAuthenticatedId());

        orderItemRepository.save(newOrderItem);
        evictCachesForTableBookingId(tableBookingId);

        log.info("Order item added successfully to booking ID: {}", tableBookingId);
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

    @Cacheable(
            value = "restaurant:tableBookings",
            key = "#filter.hashCode()",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public CustomPageDto<TableBookingDtoWoRestaurant> getTableBookingDetails(TableBookingFilter filter) {
        log.info("Fetching table booking details for restaurantId: {}", filter.restaurantId());

        Sort sort = filter.sortDir().equalsIgnoreCase("asc")
                ?Sort.by(filter.sortBy()).ascending():Sort.by(filter.sortBy()).descending();

        log.info("Creating pageable object");
        Pageable pageable = PageRequest.of(filter.pageNo(), filter.pageSize(), sort);

        Specification<TableBooking> spec = Specification.<TableBooking>allOf()
                .and(tableBookingSpecification.hasRestaurantId(filter.restaurantId()))
                .and(tableBookingSpecification.hasTableNumber(filter.tableNumber()))
                .and(tableBookingSpecification.hasBookingDate(filter.bookingDate()))
                .and(tableBookingSpecification.hasDineInTime(filter.dineInTime()))
                .and(tableBookingSpecification.hasDuration(filter.duration()))
                .and(tableBookingSpecification.hasNoOfGuest(filter.noOfGuest()))
                .and(tableBookingSpecification.hasBookingStatus(filter.bookingStatus()))
                .and(tableBookingSpecification.hasDishStatus(filter.dishStatus()));

        Page<TableBooking> bookingsPage = tableBookingRepository.findAll(spec, pageable);

        if (!bookingsPage.hasContent()){
            log.info("No bookings found for the given criteria in restaurant ID: {}", filter.restaurantId());
            return new CustomPageDto<>(Page.empty());
        }

        Page<TableBookingDtoWoRestaurant> bookingDtosPage = bookingsPage.map(tableBooking ->
                tableBookingMapper.toDtoWoRestaurant(tableBooking,filter.restaurantId()));

        log.info("Fetched {} bookings for restaurantId: {}", bookingDtosPage.getTotalElements(), filter.restaurantId());
        return new CustomPageDto<>(bookingDtosPage);
    }

    @CacheEvict(value = "booking:pages", allEntries = true)
    @Transactional
    public void updateBookingStatus(UUID tableBookingId, TableBookingStatusUpdateRequest statusUpdateRequest) {
        log.info("Updating booking status for booking ID: {}", tableBookingId);

        TableBooking existingBooking = tableBookingRepository.findByIdWithGuestInformation(tableBookingId)
                .orElseThrow(() -> new TableBookingException("Booking not found with ID: " + tableBookingId));

        UUID userId=existingBooking.getGuestInformation().getUserId();

        if (statusUpdateRequest.getBookingStatus()!=null) {
            log.info("Updating booking status from {} to {}", existingBooking.getBookingStatus(), statusUpdateRequest.getBookingStatus());
            existingBooking.setBookingStatus(BookingStatus.valueOf(statusUpdateRequest.getBookingStatus().toUpperCase(Locale.ROOT)));
            sendNotificationViaKafka(userId, statusUpdateRequest.getBookingStatus(),existingBooking,true);
        }

        if (statusUpdateRequest.getDishStatus()!=null) {
            log.info("Updating dish status from {} to {}", existingBooking.getDishStatus(), statusUpdateRequest.getDishStatus());
            existingBooking.setDishStatus(DishStatus.valueOf(statusUpdateRequest.getDishStatus().toUpperCase(Locale.ROOT)));
            sendNotificationViaKafka(userId, statusUpdateRequest.getDishStatus(),existingBooking,false);
        }

        existingBooking.setLastModifiedBy(authService.getAuthenticatedId());
        tableBookingRepository.save(existingBooking);

        evictCachesForTableBookingId(tableBookingId);
        log.info("Booking status updated successfully for booking ID: {}", tableBookingId);
    }

    private void sendNotificationViaKafka(UUID userId, String bookingStatus,
                                          TableBooking existingBooking,boolean isBookingStatus) {

        log.info("Sending notification via Kafka for userId: {} with bookingStatus: {}", userId, bookingStatus);

        kafkaService.sendEmailNotification(userId, bookingStatus, "booking-confirmation",existingBooking,isBookingStatus)
                .thenAccept(success -> {
                        if (success) {
                            log.info("Email notification sent successfully for userId: {}", userId);
                        } else {
                            log.error("Failed to send email notification for userId: {}", userId);
                        }
                });
    }

    @Transactional
    public void updateBookingDetails(UUID tableBookingId, TableBookingDetailsUpdateRequest detailsUpdateRequest) {
        log.info("Updating booking details for booking ID: {}", tableBookingId);

        TableBooking existingBooking = tableBookingRepository.findByIdAndIsActive(tableBookingId)
                .orElseThrow(() -> new TableBookingException("Booking not found with ID: " + tableBookingId));

        if (detailsUpdateRequest.getActualDineInTime()!=null) {
            log.info("Updating Dine In Time ");
            existingBooking.setActualDineInTime(detailsUpdateRequest.getActualDineInTime());
        }

        if (detailsUpdateRequest.getActualDineOutTime()!=null) {
            log.info("Updating Dine Out Time ");
            existingBooking.setActualDineOutTime(detailsUpdateRequest.getActualDineOutTime());
        }

        if (detailsUpdateRequest.getIsUpfrontPaid()!=null) {
            log.info("Updating Is Upfront Paid from {} to {}", existingBooking.getIsUpfrontPaid(), detailsUpdateRequest.getIsUpfrontPaid());
            existingBooking.setIsUpfrontPaid(detailsUpdateRequest.getIsUpfrontPaid());
        }

        if (detailsUpdateRequest.getIsPendingAmountPaid()!=null) {
            log.info("Updating Is Pending Amount Paid from {} to {}", existingBooking.getIsPendingAmountPaid(), detailsUpdateRequest.getIsPendingAmountPaid());
            existingBooking.setIsPendingAmountPaid(detailsUpdateRequest.getIsPendingAmountPaid());
        }

        existingBooking.setLastModifiedBy(authService.getAuthenticatedId());
        tableBookingRepository.save(existingBooking);

        evictCachesForTableBookingId(tableBookingId);
        log.info("Booking details updated successfully for booking ID: {}", tableBookingId);
    }

    public void evictCachesForTableBookingId(UUID tableBookingId) {
        Objects.requireNonNull(cacheManager.getCache("restaurant:tableBookingDetails")).evict(tableBookingId);
        Objects.requireNonNull(cacheManager.getCache("restaurant:tableBookings")).clear();
    }
}
