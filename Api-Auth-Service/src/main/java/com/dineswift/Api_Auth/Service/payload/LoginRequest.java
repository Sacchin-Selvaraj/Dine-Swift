package com.dineswift.Api_Auth.Service.payload;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
    private boolean rememberMe;
    private String loginType;
}
