package com.dineswift.userservice.model.entites;

import lombok.Getter;

@Getter
public enum RestaurantStatus {
    OPEN("Open"),
    CLOSED("Closed"),
    RENOVATION("Renovation"),
    UNDER_CONSTRUCTION("Under Construction"),
    TEMPORARILY_CLOSED("Temporarily Closed"),
    OPENING_SOON("Opening Soon"),
    CREATED("Created"), ;

    private final String displayName;

    RestaurantStatus(String displayName) {
        this.displayName = displayName;
    }

    public static RestaurantStatus fromDisplayName(String status) {
        for (RestaurantStatus rs : RestaurantStatus.values()) {
            if (rs.name().equalsIgnoreCase(status.replace(" ", "_"))) {
                return rs;
            }
        }
        throw new IllegalArgumentException("No enum constant with display name " + status);
    }
}
