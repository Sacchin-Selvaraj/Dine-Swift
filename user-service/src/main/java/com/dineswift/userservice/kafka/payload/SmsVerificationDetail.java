package com.dineswift.userservice.kafka.payload;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SmsVerificationDetail {

    private String userName;
    private String phoneNumber;
    private String token;
}
