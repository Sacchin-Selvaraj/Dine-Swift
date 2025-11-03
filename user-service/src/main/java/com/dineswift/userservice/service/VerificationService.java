package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.NotificationException;
import com.dineswift.userservice.exception.TokenException;
import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.kafka.service.KafkaService;
import com.dineswift.userservice.model.entites.TokenStatus;
import com.dineswift.userservice.model.entites.TokenType;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.model.entites.VerificationToken;
import com.dineswift.userservice.model.request.EmailUpdateRequest;
import com.dineswift.userservice.model.request.PasswordChangeRequest;
import com.dineswift.userservice.model.request.PhoneNumberUpdateRequest;
import com.dineswift.userservice.model.request.VerifyTokenRequest;
import com.dineswift.userservice.notification.service.SmsService;
import com.dineswift.userservice.repository.UserRepository;
import com.dineswift.userservice.repository.VerificationRepository;
import com.dineswift.userservice.security.service.AuthService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final UserCommonService userCommonService;
    private final VerificationRepository verificationRepository;
    private final KafkaService kafkaService;
    private final UserRepository userRepository;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;


    public String updateEmail(UUID userId, EmailUpdateRequest emailUpdateRequest) {

        if (userRepository.existsByEmail(emailUpdateRequest.getEmail())){
            throw new UserException("Email already registered by another user");
        }
        User user=userCommonService.findValidUser(userId);

        String token=userCommonService.generateNumericCode(6);

        VerificationToken verificationToken=setVerificationToken(token,user,TokenType.EMAIL_VERIFICATION);
        verificationToken.setNewEmail(emailUpdateRequest.getEmail());

        verificationRepository.save(verificationToken);

        kafkaService.sendEmailVerification(emailUpdateRequest.getEmail(),token,user.getUsername(),"email-verification").thenApply(status->{
            if (!status){
                verificationToken.setTokenStatus(TokenStatus.FAILED);
                throw new NotificationException("Failed to send Email");
            }else {
                verificationToken.setTokenStatus(TokenStatus.SENT);
            }
            return verificationRepository.save(verificationToken);
        });

        return "Email Sent Successfully";
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
        verificationToken.setTokenStatus(TokenStatus.VERIFIED);
        verificationRepository.save(verificationToken);

    }

    public String updatePhoneNumber(UUID userId, PhoneNumberUpdateRequest phoneNumberUpdateRequest) {

        if (userRepository.existsByPhoneNumber(phoneNumberUpdateRequest.getPhoneNumber())){
            throw new UserException("Phone Number already registered by another user");
        }

        User user=userCommonService.findValidUser(userId);

        String token=userCommonService.generateNumericCode(6);

        VerificationToken verificationToken = setVerificationToken(token, user,TokenType.PHONE_VERIFICATION);
        verificationToken.setNewPhonenumber(phoneNumberUpdateRequest.getPhoneNumber());
        verificationToken.setTokenStatus(TokenStatus.PENDING);
        verificationRepository.save(verificationToken);

        kafkaService.sendSmsVerification(phoneNumberUpdateRequest.getPhoneNumber(),token,user.getUsername()).thenApply(status->{
            if (!status){
                verificationToken.setTokenStatus(TokenStatus.FAILED);
                throw new NotificationException("Failed to send SMS");
            }else {
                verificationToken.setTokenStatus(TokenStatus.SENT);
            }
            return verificationRepository.save(verificationToken);
        });
        return "Verification Code Sent Successfully";
    }

    private static VerificationToken setVerificationToken(String token, User user,TokenType tokenType) {
        VerificationToken verificationToken=new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setTokenStatus(TokenStatus.PENDING);
        verificationToken.setTokenType(tokenType);
        verificationToken.setTokenExpiryDate(LocalDateTime.now().plusMinutes(10));
        verificationToken.setCreatedAt(LocalDateTime.now());
        return verificationToken;
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
        verificationToken.setTokenStatus(TokenStatus.VERIFIED);
        verificationRepository.save(verificationToken);
    }

    public String forgetPassword(UUID userId, String typeOfVerification) {
        log.info("Initiating forget password process for userId: {}", userId);

        User user=userCommonService.findValidUser(userId);

        String token = userCommonService.generateNumericCode(6);
        VerificationToken verificationToken = setVerificationToken(token,user,TokenType.FORGOT_PASSWORD);

        log.info("Generated verification token for userId: {}", userId);
        verificationRepository.save(verificationToken);

        if (typeOfVerification.equalsIgnoreCase("Email")){
            kafkaService.sendEmailVerification(user.getEmail(),token,user.getUsername(),"forget-password").thenApply(status->{
                if (!status){
                    verificationToken.setTokenStatus(TokenStatus.FAILED);
                    throw new NotificationException("Failed to send Email");
                }else {
                    verificationToken.setTokenStatus(TokenStatus.SENT);
                }
                return verificationRepository.save(verificationToken);
            });
            log.info("Sent forget password email to userId: {}", userId);
        }else {
            if (user.getPhoneNumber()==null){
                throw new UserException("User does not have a phone number associated");
            }
            CompletableFuture<Boolean> smsStatus = kafkaService.sendSmsVerification(user.getPhoneNumber(), token, user.getUsername());
            smsStatus.thenApply(status->{
                if (!status){
                    verificationToken.setTokenStatus(TokenStatus.FAILED);
                    throw new NotificationException("Failed to send SMS");
                }else {
                    verificationToken.setTokenStatus(TokenStatus.SENT);
                }
                return verificationRepository.save(verificationToken);
            });
            log.info("Sent forget password SMS to userId: {}", userId);
        }

        return "Verification code sent via " + typeOfVerification.toLowerCase();
    }

    public String verifyForgetPassword(UUID userId, PasswordChangeRequest passwordChangeRequest) {

        log.info("Verifying forget password token for userId: {}", userId);
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())){
            throw new UserException("New Password and Confirm Password do not match");
        }

        VerificationToken verificationToken=verificationRepository.findByToken(passwordChangeRequest.getToken())
                .orElseThrow(()->new TokenException("Verification Token was invalid"));

        if(verificationToken.getTokenType()!=TokenType.FORGOT_PASSWORD){
            throw new TokenException("Invalid Token Type");
        }
        if (verificationToken.getTokenExpiryDate().isBefore(LocalDateTime.now())){
            throw new TokenException("Verification Token was expired");
        }
        User user=verificationToken.getUser();

        if (!user.getUserId().equals(userId)){
            throw new UserException("Invalid Verification Token for the user");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));

        verificationToken.setWasUsed(true);
        verificationToken.setTokenStatus(TokenStatus.VERIFIED);
        verificationRepository.save(verificationToken);
        log.info("Password updated successfully for userId: {}", userId);
        return "Password updated successfully";

    }
}
