package com.dineswift.restaurant_service.service;


import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.model.RestaurantTable;
import com.dineswift.restaurant_service.model.TableBooking;
import com.dineswift.restaurant_service.payload.request.table.CheckAvailableSlots;
import com.dineswift.restaurant_service.payload.response.table.AvailableSlots;
import com.dineswift.restaurant_service.payload.response.table.AvailableTimeSlot;
import com.dineswift.restaurant_service.projection.RestaurantTimings;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import com.dineswift.restaurant_service.repository.TableBookingRepository;
import com.dineswift.restaurant_service.repository.TableRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final TableRepository tableRepository;
    private final TableBookingRepository tableBookingRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<AvailableSlots> getAvailableSlots(UUID restaurantId, CheckAvailableSlots checkAvailableSlots) {

        if (!restaurantRepository.existsById(restaurantId)){
            log.error("Restaurant not found with ID: {}", restaurantId);
            throw new RestaurantException("Restaurant not found with ID: " + restaurantId);
        }
        RestaurantTimings restaurantTimings = restaurantRepository.findRestaurantTimingsById(restaurantId);

        List<RestaurantTable> restaurantTables = tableRepository.findByRestaurantIdAndIsActiveTrue(restaurantId);

        List<AvailableSlots> availableSlots = restaurantTables.stream()
                .map(table->getAvailableSlot(table,restaurantTimings,checkAvailableSlots)).toList();

        log.info("Available slots fetched for restaurant with ID: {}", restaurantId);
        return availableSlots;
    }

    @Transactional(readOnly = true)
    public AvailableSlots getAvailableSlot(RestaurantTable restaurantTable,
                                           RestaurantTimings restaurantTimings,
                                           CheckAvailableSlots checkAvailableSlots) {

        log.info("Fetching available slots for the table with ID: {}", restaurantTable.getTableId());
        List<AvailableTimeSlot> availableTimeSlots;

        if (checkAvailableSlots.getNumberOfGuests()>restaurantTable.getTotalNumberOfSeats()){
            log.error("Number of guests exceeds table capacity for table ID: {}", restaurantTable.getTableId());
            return getEmptyAvailableSlots(restaurantTable);
        }

        List<TableBooking> activeBookingsForDate = tableBookingRepository
                .findByRestaurantTableAndIsActiveAndBookingDate(restaurantTable,checkAvailableSlots.getReservationDate());

        if (activeBookingsForDate.isEmpty()){
            log.info("No bookings found for the table with ID: {}. The table is fully available.", restaurantTable.getTableId());
            return createFullyAvailableSlot(restaurantTable, restaurantTimings, checkAvailableSlots);
        }

        availableTimeSlots = getAvailableTimeSlots(restaurantTable,restaurantTimings, activeBookingsForDate,checkAvailableSlots);

        AvailableSlots availableSlots = getEmptyAvailableSlots(restaurantTable);

        Long durationInMinutes = checkAvailableSlots.getDurationInMinutes();
        long MIN_SLOT_DURATION = 5;
        boolean shouldDivide = durationInMinutes != null && durationInMinutes >= MIN_SLOT_DURATION;

        if (!shouldDivide)
            availableSlots.setAvailableTimeSlots(availableTimeSlots);
        else
            availableSlots.setAvailableTimeSlots(divideSlotsByDuration(availableTimeSlots, durationInMinutes));

        log.debug("Available slots found for the table with ID: {}", restaurantTable.getTableId());

        return availableSlots;
    }

    private List<AvailableTimeSlot> divideSlotsByDuration(List<AvailableTimeSlot> availableTimeSlots,
                                                          Long durationInMinutes) {

        List<AvailableTimeSlot> dividedSlotsByDuration = new ArrayList<>();

        log.debug("Dividing available time slots into segments of {} minutes", durationInMinutes);

        for (AvailableTimeSlot slot : availableTimeSlots) {
            LocalTime slotStartTime = slot.getStartTime();
            LocalTime slotEndTime = slot.getEndTime();

            while (!slotStartTime.plusMinutes(durationInMinutes).isAfter(slotEndTime)) {
                LocalTime gapEndTime = slotStartTime.plusMinutes(durationInMinutes);
                if (gapEndTime.isBefore(slot.getStartTime())) {
                    break;
                }
                addDividedSlot( slot, slotStartTime, gapEndTime, dividedSlotsByDuration);
                slotStartTime = gapEndTime;

            }

            LocalTime gapEndTime = slotStartTime.plusMinutes(durationInMinutes);
            if (gapEndTime.isAfter(slotEndTime) && slotStartTime.isBefore(slotEndTime)) {
                addDividedSlot( slot, slotStartTime, slotEndTime, dividedSlotsByDuration);
            }
        }
        return dividedSlotsByDuration;
    }

    public List<AvailableTimeSlot> getAvailableTimeSlots(RestaurantTable restaurantTable,
                                                         RestaurantTimings restaurantTimings,
                                                         List<TableBooking> activeBookingsForDate,
                                                         CheckAvailableSlots checkAvailableSlots) {

        log.debug("Calculating available time slots for table ID: {}", restaurantTable.getTableId());

        activeBookingsForDate.sort(Comparator.comparing(TableBooking::getDineInTime));

        LocalTime startTime = getStartTime(restaurantTimings, checkAvailableSlots);
        LocalTime endTime = restaurantTimings.getClosingTime();
        final int BUFFER_MINUTES = 5;

        List<AvailableTimeSlot> availableTimeSlots=new ArrayList<>();
        log.debug("Finding gaps between bookings for available time slots.");

        for (TableBooking booking : activeBookingsForDate) {
            LocalTime bookingStartTime = booking.getDineInTime();
            LocalTime bookingEndTime = booking.getDineOutTime();

            if (startTime.isBefore(bookingStartTime)){
                LocalTime slotEndTime = bookingStartTime.minusMinutes(BUFFER_MINUTES);
                if (startTime.isBefore(slotEndTime)){

                AvailableTimeSlot availableTimeSlot = createAvailableTimeSlot(startTime,
                        slotEndTime, restaurantTable.getTotalNumberOfSeats());

                availableTimeSlots.add(availableTimeSlot);
                }
            }
            startTime=bookingEndTime.plusMinutes(BUFFER_MINUTES);

        }
        if (startTime.isBefore(endTime)){
            AvailableTimeSlot availableTimeSlot = createAvailableTimeSlot(startTime,
                    endTime, restaurantTable.getTotalNumberOfSeats());

            availableTimeSlots.add(availableTimeSlot);
        }
        return availableTimeSlots;
    }

    private static AvailableSlots getEmptyAvailableSlots(RestaurantTable restaurantTable) {
        log.debug("Creating empty AvailableSlots for table ID: {}", restaurantTable.getTableId());

        AvailableSlots availableSlots = new AvailableSlots();
        availableSlots.setTableId(restaurantTable.getTableId());
        availableSlots.setTableNumber(restaurantTable.getTableNumber());
        availableSlots.setTableDescription(restaurantTable.getTableDescription());
        availableSlots.setTableShape(restaurantTable.getTableShape());
        availableSlots.setTotalNumberOfSeats(restaurantTable.getTotalNumberOfSeats());
        availableSlots.setAvailableTimeSlots(new ArrayList<>());
        return availableSlots;
    }
    private AvailableSlots createFullyAvailableSlot(RestaurantTable restaurantTable,
                                                    RestaurantTimings restaurantTimings,
                                                    CheckAvailableSlots checkAvailableSlots) {

        log.debug("Creating fully available slot for table ID: {}", restaurantTable.getTableId());
        AvailableSlots availableSlots = getEmptyAvailableSlots(restaurantTable);

        AvailableTimeSlot availableTimeSlot = new AvailableTimeSlot();
        LocalTime startTime = getStartTime(restaurantTimings, checkAvailableSlots);
        LocalTime endTime = restaurantTimings.getClosingTime();

        availableTimeSlot.setStartTime(startTime);
        availableTimeSlot.setEndTime(endTime);

        long durationMinutes = Duration.between(startTime, endTime).toMinutes();
        availableTimeSlot.setSlotDurationMinutes(durationMinutes);
        availableTimeSlot.setNumberOfAvailableSeats(restaurantTable.getTotalNumberOfSeats());

        List<AvailableTimeSlot> availableTimeSlots = new ArrayList<>();

        log.debug("Table with ID: {} is fully available from {} to {}", restaurantTable.getTableId(),
                startTime, endTime);

       if (checkAvailableSlots.getDurationInMinutes()==null)
           availableTimeSlots.add(availableTimeSlot);
       else
           availableTimeSlots = divideSlotsByDuration(List.of(availableTimeSlot),
                   checkAvailableSlots.getDurationInMinutes());

        availableSlots.setAvailableTimeSlots(availableTimeSlots);

        return availableSlots;
    }

    private static LocalTime getStartTime(RestaurantTimings restaurantTimings,
                                          CheckAvailableSlots checkAvailableSlots) {
        return restaurantTimings.getOpeningTime()
                .isBefore(checkAvailableSlots.getReservationTime())
                ? checkAvailableSlots.getReservationTime()
                : restaurantTimings.getOpeningTime();
    }

    private AvailableTimeSlot createAvailableTimeSlot(LocalTime startTime,
                                                      LocalTime slotEndTime,
                                                      Integer totalNumberOfSeats) {

        log.info("Creating AvailableTimeSlot from {} to {}", startTime, slotEndTime);
        AvailableTimeSlot availableTimeSlot = new AvailableTimeSlot();
        availableTimeSlot.setStartTime(startTime);
        availableTimeSlot.setEndTime(slotEndTime);
        long durationMinutes = Duration.between(startTime, slotEndTime).toMinutes();
        availableTimeSlot.setSlotDurationMinutes(durationMinutes);
        availableTimeSlot.setNumberOfAvailableSeats(totalNumberOfSeats);
        return availableTimeSlot;
    }

    private static void addDividedSlot(AvailableTimeSlot slot,
                                       LocalTime slotStartTime,
                                       LocalTime gapEndTime,
                                       List<AvailableTimeSlot> dividedSlotsByDuration) {

        log.debug("Adding divided slot from {} to {}", slotStartTime, gapEndTime);

        long MIN_SLOT_DURATION = 15;
        long durationMinutes = Duration.between(slotStartTime, gapEndTime).toMinutes();

        if (durationMinutes<MIN_SLOT_DURATION)
            return;

        AvailableTimeSlot dividedSlot = new AvailableTimeSlot();
        dividedSlot.setStartTime(slotStartTime);
        dividedSlot.setEndTime(gapEndTime);
        dividedSlot.setSlotDurationMinutes(durationMinutes);
        dividedSlot.setNumberOfAvailableSeats(slot.getNumberOfAvailableSeats());
        dividedSlotsByDuration.add(dividedSlot);
    }
}
