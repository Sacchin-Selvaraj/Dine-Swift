package com.dineswift.Api_Auth.Service.service;

import com.dineswift.Api_Auth.Service.payload.LoginRequest;
import com.dineswift.Api_Auth.Service.payload.RoleDto;
import com.dineswift.Api_Auth.Service.payload.RoleName;
import com.dineswift.Api_Auth.Service.payload.UserResponse;
import com.dineswift.Api_Auth.Service.utilities.JwtUtilities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final RestClient restClient;
    private final JwtUtilities jwtUtilities;

    public String authenticateUser(LoginRequest loginRequest) {

        UserResponse userResponse = getResponseFromUserService(loginRequest);
        String token="";
        if (userResponse!=null){
            log.info("User authenticated successfully with User Service");
            Map<String,Object> claims = new HashMap<>();
            claims.put("authId", userResponse.getUserId());
            claims.put("roles", getRoleName(userResponse));
            token = jwtUtilities.generateToken(claims,userResponse.getUsername());

        }else {
            log.error("User authentication failed with User Service");
            return "Invalid email or password";
        }
        return token;
    }

    private List<RoleName> getRoleName(UserResponse userResponse) {
        return userResponse.getRoles().stream()
                .map(RoleDto::getRoleName)
                .toList();
    }

    private UserResponse getResponseFromUserService(LoginRequest loginRequest) {
        log.info("Sending login request to User Service for email: {}", loginRequest.getEmail());
        ResponseEntity<UserResponse> userResponse = restClient.post()
                .uri("/user/login")
                .body(loginRequest)
                .retrieve()
                .toEntity(UserResponse.class);
        log.info("Received response from User Service: {}", userResponse.getBody());
        return userResponse.getBody();
    }
}
