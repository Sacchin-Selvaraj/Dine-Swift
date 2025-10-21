package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.TableBooking;
import com.dineswift.restaurant_service.payload.response.tableBooking.GuestInformationDto;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TableBookingMapper {

    private final ModelMapper modelMapper;
    private final TableMapper tableMapper;
    private final RestaurantMapper restaurantMapper;

    public TableBookingDto toDto(TableBooking existingBooking) {

        log.info("Mapping TableBooking entity to TableBookingDto");
        TableBookingDto tableBookingDto = modelMapper.map(existingBooking,TableBookingDto.class);

        log.info("Restaurant Table mapping to RestaurantTableDto");
        tableBookingDto.setRestaurantTableDto( tableMapper.toDto(existingBooking.getRestaurantTable()));

        log.info("Restaurant mapping to RestaurantDto");
        tableBookingDto.setRestaurantDto( restaurantMapper.toDTO(existingBooking.getRestaurant()));

        log.info("Mapping for Guest Information");
        tableBookingDto.setGuestInformationDto(modelMapper.map(existingBooking.getGuestInformation(), GuestInformationDto.class));

        log.info("Mapping completed for TableBookingDto");
        return tableBookingDto;

    }
}
