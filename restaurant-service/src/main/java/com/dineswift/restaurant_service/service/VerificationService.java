package com.dineswift.restaurant_service.service;

import com.dineswift.userservice.exception.TokenException;
import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.TokenType;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.model.entites.VerificationToken;
import com.dineswift.userservice.model.request.EmailUpdateRequest;
import com.dineswift.userservice.model.request.PhoneNumberUpdateRequest;
import com.dineswift.userservice.model.request.VerifyTokenRequest;
import com.dineswift.userservice.repository.UserRepository;
import com.dineswift.userservice.repository.VerificationRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class VerificationService {

    private final UserCommonService userCommonService;
    private final VerificationRepository verificationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final SmsService smsService;

    public VerificationService(UserCommonService userCommonService, VerificationRepository verificationRepository, EmailService emailService, UserRepository userRepository, SmsService smsService) {
        this.userCommonService = userCommonService;
        this.verificationRepository = verificationRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.smsService = smsService;
    }

    public CompletableFuture<String> updateEmail(UUID userId, EmailUpdateRequest emailUpdateRequest) {

        if (userRepository.existsByEmail(emailUpdateRequest.getEmail())){
            throw new UserException("Email already registered by another user");
        }
        User user=userCommonService.findValidUser(userId);

        String token=userCommonService.generateNumericCode(6);

        Map<String,Object> modal = new HashMap<>();
        modal.put("userName", user.getUsername());
        modal.put("companyName", "DineSwift");
        modal.put("verificationCode", token);
        modal.put("expiryTime", 10);

        VerificationToken verificationToken=new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setNewEmail(emailUpdateRequest.getEmail());
        verificationToken.setUser(user);
        verificationToken.setTokenType(TokenType.EMAIL_VERIFICATION);
        verificationToken.setTokenExpiryDate(LocalDateTime.now().plusMinutes(10));
        verificationToken.setCreatedAt(LocalDateTime.now());

        emailService.sendMail(emailUpdateRequest.getEmail(),"Email Verification Code from DineSwift","email-verification",modal);

        verificationRepository.save(verificationToken);

        return CompletableFuture.completedFuture("Email Sent Successfully");
    }


    public void verifyEmail(UUID userId, @Valid VerifyTokenRequest verifyEmailRequest) {

        VerificationToken verificationToken=verificationRepository.findByToken(verifyEmailRequest.getToken())
                .orElseThrow(()->new TokenException("Email Verification Token was invalid"));

        if(verificationToken.getTokenType()!=TokenType.EMAIL_VERIFICATION){
            throw new TokenException("Invalid Token Type");
        }
        if (verificationToken.getTokenExpiryDate().isBefore(LocalDateTime.now())){
            throw new TokenException("Email Verification Token was expired");
        }
        User user=verificationToken.getUser();
        if (!user.getUserId().equals(userId)){
            throw new UserException("Invalid Verification Token");
        }
        user.setEmail(verificationToken.getNewEmail());

        verificationToken.setWasUsed(true);
        verificationRepository.save(verificationToken);

    }

    public CompletableFuture<String> updatePhoneNumber(UUID userId, PhoneNumberUpdateRequest phoneNumberUpdateRequest) {

        if (userRepository.existsByPhoneNumber(phoneNumberUpdateRequest.getPhoneNumber())){
            throw new UserException("Phone Number already registered by another user");
        }

        User user=userCommonService.findValidUser(userId);

        String token=userCommonService.generateNumericCode(6);

        VerificationToken verificationToken=new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setNewPhonenumber(phoneNumberUpdateRequest.getPhoneNumber());
        verificationToken.setUser(user);
        verificationToken.setTokenType(TokenType.PHONE_VERIFICATION);
        verificationToken.setTokenExpiryDate(LocalDateTime.now().plusMinutes(10));
        verificationToken.setCreatedAt(LocalDateTime.now());

        smsService.sendSms(phoneNumberUpdateRequest.getPhoneNumber(),"Verification Code for "+user.getUsername()+" is: "+token);
        verificationRepository.save(verificationToken);

        return CompletableFuture.completedFuture("Verification Code Sent Successfully");
    }

    public void verifyPhoneNumber(UUID userId, VerifyTokenRequest verifyPhoneNumberRequest) {

        VerificationToken verificationToken=verificationRepository.findByToken(verifyPhoneNumberRequest.getToken())
                .orElseThrow(()->new TokenException("Verification Token was invalid"));

        if(verificationToken.getTokenType()!=TokenType.PHONE_VERIFICATION){
            throw new TokenException("Invalid Token Type");
        }
        if (verificationToken.getTokenExpiryDate().isBefore(LocalDateTime.now())){
            throw new TokenException("Verification Token was expired");
        }
        User user=verificationToken.getUser();
        if (!user.getUserId().equals(userId)){
            throw new UserException("Invalid Verification Token");
        }
        user.setPhoneNumber(verificationToken.getNewPhonenumber());

        verificationToken.setWasUsed(true);

        verificationRepository.save(verificationToken);
    }
}
