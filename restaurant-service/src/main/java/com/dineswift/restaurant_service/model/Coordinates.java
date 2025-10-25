package com.dineswift.restaurant_service.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class Coordinates {

    private BigDecimal latitude;
    private BigDecimal longitude;

}
