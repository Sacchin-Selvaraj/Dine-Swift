package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.employee.*;
import com.dineswift.restaurant_service.payload.response.MessageResponse;
import com.dineswift.restaurant_service.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/update-mail/{employeeId}")
    public ResponseEntity<String> updateEmail(@PathVariable UUID employeeId, @Valid @RequestBody EmailUpdateRequest emailUpdateRequest) {
        String response=verificationService.updateEmail(employeeId,emailUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-mail/{employeeId}")
    public ResponseEntity<String> verifyEmail(@PathVariable UUID employeeId, @Valid @RequestBody VerifyTokenRequest verifyEmailRequest){
        verificationService.verifyEmail(employeeId,verifyEmailRequest);
        return ResponseEntity.ok("Email have been updated Successfully");
    }

    @PostMapping("/update-phone-number/{employeeId}")
    public ResponseEntity<String> updatePhoneNumber(@PathVariable UUID employeeId, @Valid @RequestBody PhoneNumberUpdateRequest phoneNumberUpdateRequest) {
        String response=verificationService.updatePhoneNumber(employeeId,phoneNumberUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-phone-number/{employeeId}")
    public ResponseEntity<String> verifyPhoneNumber(@PathVariable UUID employeeId, @Valid @RequestBody VerifyTokenRequest verifyPhoneNumberRequest){
        verificationService.verifyPhoneNumber(employeeId,verifyPhoneNumberRequest);
        return ResponseEntity.ok("PhoneNumber have been updated Successfully");
    }

    @PostMapping("/forget-password/{employeeId}" )
    public ResponseEntity<MessageResponse> forgetPassword(@PathVariable UUID employeeId, @RequestParam String typeOfVerification) {
        String response=verificationService.forgetPassword(employeeId,typeOfVerification);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }

    @PostMapping("/verify-forget-password/{employeeId}" )
    public ResponseEntity<MessageResponse> verifyForgetPassword(@PathVariable UUID employeeId, @Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        String response = verificationService.verifyForgetPassword(employeeId, passwordUpdateRequest);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }

}
