package com.dineswift.userservice.model.response.booking;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RestaurantTableDto {

    private UUID tableId;

    private String tableNumber;

    private String tableDescription;

    private Integer totalNumberOfSeats;

    private String tableShape;

    private ZonedDateTime createdDate;

    private UUID createdBy;

    private ZonedDateTime lastModifiedDate;

    private UUID lastModifiedBy;
}
