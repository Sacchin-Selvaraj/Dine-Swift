package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.request.EmailUpdateRequest;
import com.dineswift.userservice.model.request.PhoneNumberUpdateRequest;
import com.dineswift.userservice.model.request.VerifyTokenRequest;
import com.dineswift.userservice.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/user/verification")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/update-mail")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> updateEmail(@Valid @RequestBody EmailUpdateRequest emailUpdateRequest) throws ExecutionException, InterruptedException {
        String response=verificationService.updateEmail(emailUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-mail")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody VerifyTokenRequest verifyEmailRequest){
        verificationService.verifyEmail(verifyEmailRequest);
        return ResponseEntity.ok("Email have been updated Successfully");
    }

    @PostMapping("/update-phone-number")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> updatePhoneNumber(@Valid @RequestBody PhoneNumberUpdateRequest phoneNumberUpdateRequest) {
        String response=verificationService.updatePhoneNumber(phoneNumberUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-phone-number")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> verifyPhoneNumber(@Valid @RequestBody VerifyTokenRequest verifyPhoneNumberRequest){
        verificationService.verifyPhoneNumber(verifyPhoneNumberRequest);
        return ResponseEntity.ok("PhoneNumber have been updated Successfully");
    }

}
