package com.dineswift.restaurant_service.payload.response.tableBooking;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class GuestInformationDto {
    private UUID guestInformationId;
    private String specialRequest;
    private String guestName;
    private String contactNumber;
    private String contactEmail;
    private BigDecimal cancellationFee;
    private String cancellationReason;
    private ZonedDateTime cancellationTime;
    private UUID userId;
}
