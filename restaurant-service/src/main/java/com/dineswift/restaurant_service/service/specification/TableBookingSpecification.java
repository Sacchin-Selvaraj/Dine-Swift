package com.dineswift.restaurant_service.service.specification;

import com.dineswift.restaurant_service.exception.TableBookingException;
import com.dineswift.restaurant_service.model.*;
import com.dineswift.restaurant_service.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public final class TableBookingSpecification {

    private final TableRepository tableRepository;

    public Specification<TableBooking> hasRestaurant(Restaurant restaurant) {
        if (restaurant==null)
            return Specification.allOf();
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("restaurant"), restaurant);
    }

    public Specification<TableBooking> hasTableNumber(String tableNumber) {
        if (tableNumber == null || tableNumber.isEmpty()) {
            return Specification.allOf();
        }
        RestaurantTable restaurantTable = tableRepository.findByTableNumber(tableNumber)
                .orElseThrow(()-> new TableBookingException("Table with number "+tableNumber+" not found"));

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("restaurantTable"), restaurantTable);
    }

    public Specification<TableBooking> hasBookingDate(LocalDate bookingDate) {
        if (bookingDate == null) {
            return Specification.allOf();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("bookingDate"), bookingDate);
    }

    public Specification<TableBooking> hasDineInTime(LocalTime dineInTime) {
        if (dineInTime == null) {
            return Specification.allOf();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("dineInTime"), dineInTime);
    }

    public Specification<TableBooking> hasDuration(Integer duration) {
        if (duration == null) {
            return Specification.allOf();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("duration"), duration);
    }

    public Specification<TableBooking> hasNoOfGuest(Integer noOfGuest) {
        if (noOfGuest == null) {
            return Specification.allOf();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("noOfGuest"), noOfGuest);
    }

    public Specification<TableBooking> hasBookingStatus(String bookingStatus) {
        if (bookingStatus == null || bookingStatus.isEmpty()) {
            return Specification.allOf();
        }
        BookingStatus bookingStatusAsEnum;
        try {
            bookingStatusAsEnum = BookingStatus.valueOf(bookingStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TableBookingException("Invalid booking status: " + bookingStatus);
        }

        final BookingStatus finalStatus = bookingStatusAsEnum;
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("bookingStatus"), finalStatus);
    }

    public Specification<TableBooking> hasDishStatus(String dishStatus) {
        if (dishStatus == null || dishStatus.isEmpty()) {
            return Specification.allOf();
        }
        DishStatus dishStatusAsEnum;
        try {
            dishStatusAsEnum = DishStatus.valueOf(dishStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TableBookingException("Invalid dish status: " + dishStatus);
        }

        final DishStatus finalDishStatus = dishStatusAsEnum;
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("dishStatus"), finalDishStatus);
    }


    public Specification<TableBooking> hasRestaurantId(UUID restaurantId) {
        if (restaurantId == null) {
            return Specification.allOf();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("restaurant").get("restaurantId"), restaurantId);
    }
}
