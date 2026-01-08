package com.dineswift.Api_Auth.Service.controller;


import com.dineswift.Api_Auth.Service.payload.LoginRequest;
import com.dineswift.Api_Auth.Service.payload.LoginResponse;
import com.dineswift.Api_Auth.Service.payload.MessageResponse;
import com.dineswift.Api_Auth.Service.payload.TokenResponse;
import com.dineswift.Api_Auth.Service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Mono<LoginResponse>> loginRequest(@RequestBody LoginRequest loginRequest,
                                                            ServerHttpResponse response){
        Mono<LoginResponse> authToken = authService.authenticateUser(loginRequest,response);
        log.info("Generated Auth Token: {}", authToken);
        return ResponseEntity.ok(authToken);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Mono<TokenResponse>> refreshToken(@CookieValue(name = "refreshToken") String refreshToken,
                                                            ServerHttpResponse response){
        log.info("Received request to refresh token");
        Mono<TokenResponse> newAuthToken = authService.refreshAuthToken(refreshToken,response);
        log.info("Generated new Auth Token: {}", newAuthToken);
        return ResponseEntity.ok(newAuthToken);
    }

    @GetMapping("/logout")
    public ResponseEntity<MessageResponse> logoutUser(ServerHttpResponse response){
        log.info("Received logout request");
        authService.logoutUser(response);
        MessageResponse messageResponse = new MessageResponse("User logged out successfully");
        return ResponseEntity.ok(messageResponse);
    }
}
