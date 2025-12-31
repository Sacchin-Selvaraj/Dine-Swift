package com.dineswift.notification_service.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@RequiredArgsConstructor
public class BookingStatusPayload {

    private String email;
    private String bookingStatusUpdate;
    private String templateType;
    private Map<String, Object> model;

}
