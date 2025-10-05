package com.dineswift.restaurant_service.payload.request.table;

import com.dineswift.restaurant_service.model.TableStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class TableCreateRequest {

    @NotBlank(message = "Table number is required")
    @Size(min = 1, max = 50, message = "Table number must be between 1 and 50 characters")
    private String tableNumber;

    @Size(max = 1000, message = "Table description cannot exceed 1000 characters")
    private String tableDescription;

    @NotNull(message = "Total number of seats is required")
    @Min(value = 1, message = "Total number of seats must be at least 1")
    @Max(value = 100, message = "Total number of seats cannot exceed 100")
    private Integer totalNumberOfSeats;

    private String tableShape;

}
