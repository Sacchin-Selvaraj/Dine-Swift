package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.request.ForgotPasswordRequest;
import com.dineswift.userservice.model.request.PasswordChangeRequest;
import com.dineswift.userservice.model.response.MessageResponse;
import com.dineswift.userservice.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/password")
public class PasswordController {

    private final VerificationService verificationService;

    @PostMapping("/forget-password" )
    public ResponseEntity<MessageResponse> forgetPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        String response=verificationService.forgetPassword(forgotPasswordRequest);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }

    @PostMapping("/verify-forget-password" )
    public ResponseEntity<MessageResponse> verifyForgetPassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        String response=verificationService.verifyForgetPassword(passwordChangeRequest);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }
}
