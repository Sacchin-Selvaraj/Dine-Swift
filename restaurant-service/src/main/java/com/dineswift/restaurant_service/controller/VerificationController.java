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
    @PostMapping("/update-mail/{employeeId}")
    public ResponseEntity<String> updateEmail(@PathVariable UUID employeeId, @Valid @RequestBody EmailUpdateRequest emailUpdateRequest) {
        String response=verificationService.updateEmail(employeeId,emailUpdateRequest);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("!hasRole('ROLE_USER')")
    @PostMapping("/verify-mail/{employeeId}")
    public ResponseEntity<String> verifyEmail(@PathVariable UUID employeeId, @Valid @RequestBody VerifyTokenRequest verifyEmailRequest){
        verificationService.verifyEmail(employeeId,verifyEmailRequest);
        return ResponseEntity.ok("Email have been updated Successfully");
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @PostMapping("/update-phone-number/{employeeId}")
    public ResponseEntity<String> updatePhoneNumber(@PathVariable UUID employeeId, @Valid @RequestBody PhoneNumberUpdateRequest phoneNumberUpdateRequest) {
        String response=verificationService.updatePhoneNumber(employeeId,phoneNumberUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @PostMapping("/verify-phone-number/{employeeId}")
    public ResponseEntity<String> verifyPhoneNumber(@PathVariable UUID employeeId, @Valid @RequestBody VerifyTokenRequest verifyPhoneNumberRequest){
        verificationService.verifyPhoneNumber(employeeId,verifyPhoneNumberRequest);
        return ResponseEntity.ok("PhoneNumber have been updated Successfully");
    }

    @PostMapping("/forget-password" )
    public ResponseEntity<MessageResponse> forgetPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        String response=verificationService.forgetPassword(forgotPasswordRequest);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }

    @PostMapping("/verify-forget-password" )
    public ResponseEntity<MessageResponse> verifyForgetPassword(@Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        String response = verificationService.verifyForgetPassword(passwordUpdateRequest);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }

}
