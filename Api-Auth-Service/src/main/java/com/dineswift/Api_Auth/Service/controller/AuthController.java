package com.dineswift.Api_Auth.Service.controller;


import com.dineswift.Api_Auth.Service.payload.LoginRequest;
import com.dineswift.Api_Auth.Service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/user")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> loginRequest(@RequestBody LoginRequest loginRequest){
        String authToken = authService.authenticateUser(loginRequest);
        log.info("Generated Auth Token: {}", authToken);
        return ResponseEntity.ok(authToken);
    }
}
