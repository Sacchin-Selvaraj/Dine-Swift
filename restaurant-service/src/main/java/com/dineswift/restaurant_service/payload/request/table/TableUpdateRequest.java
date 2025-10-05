package com.dineswift.restaurant_service.payload.request.table;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TableUpdateRequest {

    private String tableNumber;

    private String tableDescription;

    private Integer totalNumberOfSeats;

    private String tableShape;

}
