package com.dineswift.userservice.security.controller;

import com.dineswift.userservice.model.request.LoginRequest;
import com.dineswift.userservice.model.request.UserRequest;
import com.dineswift.userservice.model.response.AuthResponse;
import com.dineswift.userservice.security.service.AuthService;
import com.dineswift.userservice.security.utilities.JWTUtilities;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginException;

@RestController
@RequestMapping("/user")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody UserRequest userRequest){
        AuthResponse authResponse=authService.signupUser(userRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse=authService.signInUser(loginRequest);
        return ResponseEntity.ok(authResponse);

    }

}
