package com.dineswift.restaurant_service.model.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AuthResponse {
    private String authToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserDTO user;
}
