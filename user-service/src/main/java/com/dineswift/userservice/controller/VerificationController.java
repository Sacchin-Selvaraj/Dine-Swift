package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.request.EmailUpdateRequest;
import com.dineswift.userservice.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/update-mail/{userId}")
    public ResponseEntity<String> updateEmail(@PathVariable UUID userId, @Valid @RequestBody EmailUpdateRequest emailUpdateRequest){
        verificationService.updateEmail(userId,emailUpdateRequest);
        return ResponseEntity.ok("Email Verification Code Sent Successfully");
    }
}
