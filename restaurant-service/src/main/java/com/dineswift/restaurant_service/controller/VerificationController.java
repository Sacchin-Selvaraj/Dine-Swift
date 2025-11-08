package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.employee.*;
import com.dineswift.restaurant_service.payload.request.verification.ForgotPasswordRequest;
import com.dineswift.restaurant_service.payload.response.MessageResponse;
import com.dineswift.restaurant_service.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/restaurant/employee")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final VerificationService verificationService;

    @PreAuthorize("!hasRole('ROLE_USER')")
    @PostMapping("/update-mail")
    public ResponseEntity<MessageResponse> updateEmail(@Valid @RequestBody EmailUpdateRequest emailUpdateRequest) {
        String response=verificationService.updateEmail(emailUpdateRequest);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }
    @PreAuthorize("!hasRole('ROLE_USER')")
    @PostMapping("/verify-mail")
    public ResponseEntity<MessageResponse> verifyEmail( @Valid @RequestBody VerifyTokenRequest verifyEmailRequest){
        verificationService.verifyEmail(verifyEmailRequest);
        return ResponseEntity.ok(MessageResponse.builder().message("Email Verified Successfully").build());
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @PostMapping("/update-phone-number")
    public ResponseEntity<MessageResponse> updatePhoneNumber( @Valid @RequestBody PhoneNumberUpdateRequest phoneNumberUpdateRequest) {
        String response=verificationService.updatePhoneNumber(phoneNumberUpdateRequest);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @PostMapping("/verify-phone-number")
    public ResponseEntity<MessageResponse> verifyPhoneNumber( @Valid @RequestBody VerifyTokenRequest verifyPhoneNumberRequest){
        verificationService.verifyPhoneNumber(verifyPhoneNumberRequest);
        return ResponseEntity.ok(MessageResponse.builder().message("PhoneNumber have been updated Successfully").build());
    }

    @PostMapping("/forgot-password" )
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        String response=verificationService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }

    @PostMapping("/verify-forgot-password" )
    public ResponseEntity<MessageResponse> verifyForgotPassword(@Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        String response = verificationService.verifyforgotPassword(passwordUpdateRequest);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }

}
