package com.dineswift.notification_service.model;

import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingStatusPayload {

    private String email;
    private String bookingStatusUpdate;
    private String templateType;
    private Map<String, Object> model;

}
