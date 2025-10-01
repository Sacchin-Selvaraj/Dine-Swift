package com.dineswift.restaurant_service.kafka.payload;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SmsVerificationDetail {

    private String userName;
    private String phoneNumber;
    private String token;
}
