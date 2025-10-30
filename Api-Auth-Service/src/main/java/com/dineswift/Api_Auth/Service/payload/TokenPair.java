package com.dineswift.Api_Auth.Service.payload;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TokenPair {

    private String authToken;
    private String refreshToken;
}
