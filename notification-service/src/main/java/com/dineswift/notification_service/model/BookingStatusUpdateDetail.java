package com.dineswift.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingStatusUpdateDetail {

    private UUID userId;
    private String templateType;
    private String status;
}
