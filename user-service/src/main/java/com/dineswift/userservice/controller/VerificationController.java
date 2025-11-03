package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.request.EmailUpdateRequest;
import com.dineswift.userservice.model.request.PasswordChangeRequest;
import com.dineswift.userservice.model.request.PhoneNumberUpdateRequest;
import com.dineswift.userservice.model.request.VerifyTokenRequest;
import com.dineswift.userservice.model.response.MessageResponse;
import com.dineswift.userservice.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/user-verification")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/update-mail/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> updateEmail(@PathVariable UUID userId, @Valid @RequestBody EmailUpdateRequest emailUpdateRequest) throws ExecutionException, InterruptedException {
        String response=verificationService.updateEmail(userId,emailUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-mail/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> verifyEmail(@PathVariable UUID userId, @Valid @RequestBody VerifyTokenRequest verifyEmailRequest){
        verificationService.verifyEmail(userId,verifyEmailRequest);
        return ResponseEntity.ok("Email have been updated Successfully");
    }

    @PostMapping("/update-phone-number/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> updatePhoneNumber(@PathVariable UUID userId, @Valid @RequestBody PhoneNumberUpdateRequest phoneNumberUpdateRequest) {
        String response=verificationService.updatePhoneNumber(userId,phoneNumberUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-phone-number/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> verifyPhoneNumber(@PathVariable UUID userId, @Valid @RequestBody VerifyTokenRequest verifyPhoneNumberRequest){
        verificationService.verifyPhoneNumber(userId,verifyPhoneNumberRequest);
        return ResponseEntity.ok("PhoneNumber have been updated Successfully");
    }

}
