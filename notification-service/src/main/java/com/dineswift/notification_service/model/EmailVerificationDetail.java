package com.dineswift.notification_service.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EmailVerificationDetail {

    private String email;
    private String token;
    private String userName;
}
