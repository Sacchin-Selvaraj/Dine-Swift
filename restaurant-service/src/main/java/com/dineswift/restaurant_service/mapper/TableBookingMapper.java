package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.OrderItem;
import com.dineswift.restaurant_service.model.TableBooking;
import com.dineswift.restaurant_service.payload.response.tableBooking.GuestInformationDto;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingDto;
import com.dineswift.restaurant_service.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TableBookingMapper {

    private final ModelMapper modelMapper;
    private final TableMapper tableMapper;
    private final RestaurantMapper restaurantMapper;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;


    //Use the concurrent mapping approach to map TableBooking to TableBookingDto
    public TableBookingDto toDto(TableBooking existingBooking) {

        log.info("Mapping TableBooking entity to TableBookingDto");
        TableBookingDto tableBookingDto = modelMapper.map(existingBooking,TableBookingDto.class);

        log.info("Mapping for Guest Information");
        tableBookingDto.setGuestInformationDto(modelMapper.map(existingBooking.getGuestInformation(), GuestInformationDto.class));

        log.info("Mapping completed for TableBookingDto");
        return tableBookingDto;

    }

    public TableBookingDto toDto(TableBooking existingBooking, List<OrderItem> orderItems) {

        TableBookingDto tableBookingDto = toDto(existingBooking);
        log.info("Mapping OrderItems to OrderItemDtos for bookingId: {}", existingBooking.getTableBookingId());
        tableBookingDto.setOrderItems(orderItems.stream().map(orderItemMapper::toDtoAfterBooking).toList());
        log.info("Mapping completed for TableBookingDto with OrderItems");
        return tableBookingDto;

    }
}
