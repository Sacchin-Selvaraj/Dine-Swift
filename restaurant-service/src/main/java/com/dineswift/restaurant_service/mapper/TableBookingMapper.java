package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.TableBooking;
import com.dineswift.restaurant_service.payload.response.tableBooking.GuestInformationDto;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingDto;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingDtoWoRestaurant;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

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

        tableBookingDto.setRestaurantDto(restaurantMapper.toDTO(existingBooking.getRestaurant()));

        UUID restaurantId = existingBooking.getRestaurant().getRestaurantId();

        tableBookingDto.setRestaurantTableDto(tableMapper.toDtoWithRestaurantId(existingBooking.getRestaurantTable(), restaurantId));

        log.info("Mapping for Guest Information from TableBooking");
        tableBookingDto.setGuestInformationDto(modelMapper.map(existingBooking.getGuestInformation(), GuestInformationDto.class));

        log.info("Mapping completed for TableBookingDto");
        return tableBookingDto;

    }

    public TableBookingDtoWoRestaurant toDtoWoRestaurant(TableBooking tableBooking, UUID restaurantId) {
        log.info("Mapping TableBooking entity to TableBookingDtoWoRestaurant");
        TableBookingDtoWoRestaurant dto = modelMapper.map(tableBooking, TableBookingDtoWoRestaurant.class);

        dto.setRestaurantTableDto(tableMapper.toDtoWithRestaurantId(tableBooking.getRestaurantTable(),restaurantId));

        log.info("Mapping for Guest Information");
        dto.setGuestInformationDto(modelMapper.map(tableBooking.getGuestInformation(), GuestInformationDto.class));
        log.info("Mapping completed for TableBookingDtoWoRestaurant");
        return dto;
    }

    public TableBookingResponse toBookingResponse(TableBooking newBooking) {
        log.info("Mapping TableBooking entity to TableBookingResponse");
        TableBookingResponse bookingResponse = modelMapper.map(newBooking, TableBookingResponse.class);
        log.info("Mapping completed for TableBookingResponse");
        return bookingResponse;
    }
}
