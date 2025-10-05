package com.dineswift.restaurant_service.service;


import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantTable;
import com.dineswift.restaurant_service.model.TableBooking;
import com.dineswift.restaurant_service.payload.request.table.CheckAvailableSlots;
import com.dineswift.restaurant_service.payload.response.table.AvailableSlots;
import com.dineswift.restaurant_service.payload.response.table.AvailableTimeSlot;
import com.dineswift.restaurant_service.payload.response.table.RestaurantTableDTO;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import com.dineswift.restaurant_service.repository.TableBookingRepository;
import com.dineswift.restaurant_service.repository.TableRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReservationService {

    private final TableRepository tableRepository;
    private final TableBookingRepository tableBookingRepository;
    private final RestaurantRepository restaurantRepository;

    public List<AvailableSlots> getAvailableSlots(UUID restaurantId, CheckAvailableSlots checkAvailableSlots) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with ID: " + restaurantId));

        List<RestaurantTable> restaurantTables = tableRepository.findByRestaurantAndIsActiveTrue(restaurant);
        if (restaurantTables.isEmpty()){
            throw new RestaurantException("No active tables found for the restaurant with ID: " + restaurantId);
        }

        List<AvailableSlots> availableSlots = restaurantTables.stream().map(table->getAvailableSlot(table,restaurant,checkAvailableSlots)).toList();
        if (availableSlots.isEmpty()){
            throw new RestaurantException("No available slots found for the restaurant with provided Date: " + checkAvailableSlots.getReservationDate());
        }
        return availableSlots;
    }

    private AvailableSlots getAvailableSlot(RestaurantTable restaurantTable,Restaurant restaurant, CheckAvailableSlots checkAvailableSlots) {

        log.info("Fetching available slots for the table with ID: {}", restaurantTable.getTableId());
        AvailableSlots availableSlots = new AvailableSlots();
        availableSlots.setTableId(restaurantTable.getTableId());
        availableSlots.setTableNumber(restaurantTable.getTableNumber());
        availableSlots.setTableDescription(restaurantTable.getTableDescription());
        availableSlots.setTableShape(restaurantTable.getTableShape());
        availableSlots.setTotalNumberOfSeats(restaurantTable.getTotalNumberOfSeats());

        List<AvailableTimeSlot> availableTimeSlots=null;

        List<TableBooking> tableBookings = tableBookingRepository.findByRestaurantTableAndIsActive(restaurantTable);

        log.info("Get Available Time Slots for the table with ID: {}", restaurantTable.getTableId());
        availableTimeSlots = getAvailableTimeSlots(restaurant, tableBookings,checkAvailableSlots);
        if (availableTimeSlots.isEmpty()){
            throw new RestaurantException("No available slots found for the table with ID: " + restaurantTable.getTableId());
        }
        availableSlots.setAvailableTimeSlots(availableTimeSlots);
        log.info("Available slots found for the table with ID: {}", restaurantTable.getTableId());
        return availableSlots;
    }

    private List<AvailableTimeSlot> getAvailableTimeSlots(Restaurant restaurant, List<TableBooking> tableBookings, CheckAvailableSlots checkAvailableSlots) {

        List<AvailableTimeSlot> availableTimeSlots=new ArrayList<>();
        LocalTime startTimeFromRequest = checkAvailableSlots.getReservationTime();
        long durationInMinutes = checkAvailableSlots.getDurationInMinutes();

        for (LocalTime startTime = startTimeFromRequest;startTime.isBefore(restaurant.getClosingTime());startTime=startTime.plusMinutes(durationInMinutes)){
            if ()
        }
    }
}
