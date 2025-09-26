//package com.dineswift.restaurant_service.controller;
//
//import com.dineswift.userservice.model.request.EmailUpdateRequest;
//import com.dineswift.userservice.model.request.PhoneNumberUpdateRequest;
//import com.dineswift.userservice.model.request.VerifyTokenRequest;
//import com.dineswift.userservice.service.VerificationService;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//
//@RestController
//@RequestMapping("/user")
//public class VerificationController {
//
//    private final VerificationService verificationService;
//
//    public VerificationController(VerificationService verificationService) {
//        this.verificationService = verificationService;
//    }
//
//    @PostMapping("/update-mail/{userId}")
//    public ResponseEntity<String> updateEmail(@PathVariable UUID userId, @Valid @RequestBody EmailUpdateRequest emailUpdateRequest) throws ExecutionException, InterruptedException {
//        CompletableFuture<String> response=verificationService.updateEmail(userId,emailUpdateRequest);
//        return ResponseEntity.ok(response.get());
//    }
//
//    @PostMapping("/verify-mail/{userId}")
//    public ResponseEntity<String> verifyEmail(@PathVariable UUID userId, @Valid @RequestBody VerifyTokenRequest verifyEmailRequest){
//        verificationService.verifyEmail(userId,verifyEmailRequest);
//        return ResponseEntity.ok("Email have been updated Successfully");
//    }
//
//    @PostMapping("/update-phone-number/{userId}")
//    public ResponseEntity<String> updatePhoneNumber(@PathVariable UUID userId, @Valid @RequestBody PhoneNumberUpdateRequest phoneNumberUpdateRequest) throws ExecutionException, InterruptedException {
//        CompletableFuture<String> response=verificationService.updatePhoneNumber(userId,phoneNumberUpdateRequest);
//        return ResponseEntity.ok(response.get());
//    }
//
//    @PostMapping("/verify-phone-number/{userId}")
//    public ResponseEntity<String> verifyPhoneNumber(@PathVariable UUID userId, @Valid @RequestBody VerifyTokenRequest verifyPhoneNumberRequest){
//        verificationService.verifyPhoneNumber(userId,verifyPhoneNumberRequest);
//        return ResponseEntity.ok("PhoneNumber have been updated Successfully");
//    }
//
//
//}
