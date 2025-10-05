package com.dineswift.restaurant_service.payload.response.table;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RestaurantTableDTO {

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
