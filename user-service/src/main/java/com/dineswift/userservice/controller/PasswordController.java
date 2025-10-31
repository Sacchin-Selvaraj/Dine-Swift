package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.request.PasswordChangeRequest;
import com.dineswift.userservice.model.response.MessageResponse;
import com.dineswift.userservice.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-password")
public class PasswordController {

    private final VerificationService verificationService;

    @PostMapping("/forget-password/{userId}" )
    public ResponseEntity<MessageResponse> forgetPassword(@PathVariable UUID userId, @RequestParam String typeOfVerification) {
        String response=verificationService.forgetPassword(userId,typeOfVerification);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }

    @PostMapping("/verify-forget-password/{userId}" )
    public ResponseEntity<MessageResponse> verifyForgetPassword(@PathVariable UUID userId, @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        String response = verificationService.verifyForgetPassword(userId, passwordChangeRequest);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }
}
