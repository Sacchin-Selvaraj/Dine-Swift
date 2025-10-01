package com.dineswift.notification_service.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SmsVerificationDetail {

    private String userName;
    private String phoneNumber;
    private String token;
}
