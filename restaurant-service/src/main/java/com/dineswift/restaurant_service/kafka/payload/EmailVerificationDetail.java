package com.dineswift.restaurant_service.kafka.payload;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EmailVerificationDetail {

    private String email;
    private String token;
    private String userName;
}
