package com.dineswift.auth_service.controller;

import com.dineswift.userservice.model.request.LoginRequest;
import com.dineswift.userservice.model.request.UserRequest;
import com.dineswift.userservice.model.response.AuthResponse;
import com.dineswift.userservice.security.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @PostMapping("/update-password/{userId}")
    public ResponseEntity<String> updatePassword(@PathVariable UUID userId, @Valid @RequestBody PasswordUpdateRequest passwordRequest){
        userService.updatePassword(userId,passwordRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Password Updated Successfully");
    }

}
