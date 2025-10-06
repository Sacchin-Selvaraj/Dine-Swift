package com.dineswift.restaurant_service.service;


import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantTable;
import com.dineswift.restaurant_service.model.TableBooking;
import com.dineswift.restaurant_service.payload.request.table.CheckAvailableSlots;
import com.dineswift.restaurant_service.payload.response.table.AvailableSlots;
import com.dineswift.restaurant_service.payload.response.table.AvailableTimeSlot;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import com.dineswift.restaurant_service.repository.TableBookingRepository;
import com.dineswift.restaurant_service.repository.TableRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    public AvailableSlots getAvailableSlot(RestaurantTable restaurantTable,Restaurant restaurant, CheckAvailableSlots checkAvailableSlots) {

        log.info("Fetching available slots for the table with ID: {}", restaurantTable.getTableId());
        AvailableSlots availableSlots = new AvailableSlots();
        availableSlots.setTableId(restaurantTable.getTableId());
        availableSlots.setTableNumber(restaurantTable.getTableNumber());
        availableSlots.setTableDescription(restaurantTable.getTableDescription());
        availableSlots.setTableShape(restaurantTable.getTableShape());
        availableSlots.setTotalNumberOfSeats(restaurantTable.getTotalNumberOfSeats());

        List<AvailableTimeSlot> availableTimeSlots=new ArrayList<>();

        if (checkAvailableSlots.getNumberOfGuests()>restaurantTable.getTotalNumberOfSeats()){
            log.error("Number of guests exceeds table capacity for table ID: {}", restaurantTable.getTableId());
            return availableSlots;
        }

        List<TableBooking> tableBookings = tableBookingRepository.findByRestaurantTableAndIsActive(restaurantTable);
        if (tableBookings.isEmpty()){
            log.info("No bookings found for the table with ID: {}. The table is fully available.", restaurantTable.getTableId());
            AvailableTimeSlot availableTimeSlot = createAvailableTimeSlot(
                    restaurant.getOpeningTime().isBefore(checkAvailableSlots.getReservationTime()) ? checkAvailableSlots.getReservationTime() : restaurant.getOpeningTime(),
                    restaurant.getClosingTime(),
                    restaurantTable.getTotalNumberOfSeats()
            );
            availableTimeSlots.add(availableTimeSlot);
            availableSlots.setAvailableTimeSlots(availableTimeSlots);
            return availableSlots;
        }
        List<TableBooking> activeBookingsForDate = tableBookings.stream()
                .filter(booking -> booking.getBookingDate().isEqual(checkAvailableSlots.getReservationDate()) && booking.getIsActive())
                .sorted(Comparator.comparing(tableBooking -> tableBooking.getDineInTime().toLocalTime())).toList();

        if (activeBookingsForDate.isEmpty()){
            log.info("No bookings found for the table with ID: {}. The table is fully available.", restaurantTable.getTableId());
            AvailableTimeSlot availableTimeSlot = createAvailableTimeSlot(
                    restaurant.getOpeningTime().isBefore(checkAvailableSlots.getReservationTime()) ? checkAvailableSlots.getReservationTime() : restaurant.getOpeningTime(),
                    restaurant.getClosingTime(),
                    restaurantTable.getTotalNumberOfSeats()
            );
            availableTimeSlots.add(availableTimeSlot);
            availableSlots.setAvailableTimeSlots(availableTimeSlots);
            return availableSlots;
        }

        log.info("Get Available Time Slots for the table with ID: {}", restaurantTable.getTableId());
        availableTimeSlots = getAvailableTimeSlots(restaurantTable,restaurant, activeBookingsForDate,checkAvailableSlots);
        if (availableTimeSlots.isEmpty()){
           log.info("No available time slots found for the table with ID: {}", restaurantTable.getTableId());
           return availableSlots;
        }
        availableSlots.setAvailableTimeSlots(availableTimeSlots);
        log.info("Available slots found for the table with ID: {}", restaurantTable.getTableId());
        return availableSlots;
    }

    public List<AvailableTimeSlot> getAvailableTimeSlots(RestaurantTable restaurantTable, Restaurant restaurant, List<TableBooking> activeBookingsForDate, CheckAvailableSlots checkAvailableSlots) {
        log.info("Calculating available time slots for table ID: {}", restaurantTable.getTableId());

        LocalTime startTime = restaurant.getOpeningTime().isBefore(checkAvailableSlots.getReservationTime()) ? checkAvailableSlots.getReservationTime() : restaurant.getOpeningTime();
        LocalTime endTime = restaurant.getClosingTime();

        List<AvailableTimeSlot> availableTimeSlots= new ArrayList<>();
        for (TableBooking booking : activeBookingsForDate) {
            LocalTime bookingStartTime = booking.getDineInTime().toLocalTime();
            LocalTime bookingEndTime = booking.getDineOutTime().toLocalTime();
            if (startTime.isBefore(bookingStartTime)){
                LocalTime slotEndTime = bookingStartTime.minusMinutes(5);
                AvailableTimeSlot availableTimeSlot = createAvailableTimeSlot(startTime, slotEndTime, restaurantTable.getTotalNumberOfSeats());
                availableTimeSlots.add(availableTimeSlot);
            }
            startTime=bookingEndTime.plusMinutes(5);

        }
        if (startTime.isBefore(endTime)){
            AvailableTimeSlot availableTimeSlot = createAvailableTimeSlot(startTime, endTime, restaurantTable.getTotalNumberOfSeats());
            availableTimeSlots.add(availableTimeSlot);
        }
        return availableTimeSlots;
    }

    private AvailableTimeSlot createAvailableTimeSlot(LocalTime startTime, LocalTime slotEndTime, Integer totalNumberOfSeats) {
        AvailableTimeSlot availableTimeSlot = new AvailableTimeSlot();
        availableTimeSlot.setStartTime(startTime);
        availableTimeSlot.setEndTime(slotEndTime);
        long durationMinutes = Duration.between(startTime, slotEndTime).toMinutes();
        availableTimeSlot.setSlotDurationMinutes(durationMinutes);
        availableTimeSlot.setNumberOfAvailableSeats(totalNumberOfSeats);
        return availableTimeSlot;
    }

}
