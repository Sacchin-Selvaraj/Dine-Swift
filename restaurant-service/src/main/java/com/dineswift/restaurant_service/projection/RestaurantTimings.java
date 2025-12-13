package com.dineswift.restaurant_service.projection;

import java.time.LocalTime;

public interface RestaurantTimings {
    LocalTime getOpeningTime();
    LocalTime getClosingTime();
}
