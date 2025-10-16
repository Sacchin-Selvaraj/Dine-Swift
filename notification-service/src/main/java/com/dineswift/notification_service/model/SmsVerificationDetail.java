package com.dineswift.notification_service.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsVerificationDetail {

    private String userName;
    private String phoneNumber;
    private String token;
}
