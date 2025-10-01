package com.dineswift.userservice.kafka.payload;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EmailVerificationDetail {

    private String email;
    private String token;
    private String userName;
}
