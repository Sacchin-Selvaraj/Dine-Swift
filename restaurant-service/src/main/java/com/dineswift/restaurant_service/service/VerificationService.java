package com.dineswift.restaurant_service.service;


import com.dineswift.restaurant_service.exception.EmployeeException;
import com.dineswift.restaurant_service.exception.NotificationException;
import com.dineswift.restaurant_service.exception.TokenException;
import com.dineswift.restaurant_service.kafka.service.KafkaService;
import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.TokenStatus;
import com.dineswift.restaurant_service.model.TokenType;
import com.dineswift.restaurant_service.model.VerificationToken;
import com.dineswift.restaurant_service.payload.request.employee.EmailUpdateRequest;
import com.dineswift.restaurant_service.payload.request.employee.PasswordUpdateRequest;
import com.dineswift.restaurant_service.payload.request.employee.PhoneNumberUpdateRequest;
import com.dineswift.restaurant_service.payload.request.employee.VerifyTokenRequest;
import com.dineswift.restaurant_service.repository.EmployeeRepository;
import com.dineswift.restaurant_service.repository.VerificationRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final KafkaService kafkaService;
    private final EmployeeRepository employeeRepository;
    private final UtilityService utilityService;


    public String updateEmail(UUID employeeId, EmailUpdateRequest emailUpdateRequest) {

        if (employeeRepository.existsByEmail(emailUpdateRequest.getEmail())){
            throw new EmployeeException("Email already registered by another user");
        }
        Employee employee = employeeRepository.findByIdAndIsActive(employeeId).orElseThrow( ()->new EmployeeException("Employee not found or inactive"));

        String token=utilityService.generateNumericCode(6);

        VerificationToken verificationToken=setVerificationToken(token,employee, TokenType.EMAIL_VERIFICATION);
        verificationToken.setNewEmail(emailUpdateRequest.getEmail());

        verificationRepository.save(verificationToken);

        kafkaService.sendEmailVerification(emailUpdateRequest.getEmail(),token,employee.getEmployeeName(),"email-verification").thenApply(status->{
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


    public void verifyEmail(UUID employeeId, @Valid VerifyTokenRequest verifyEmailRequest) {

        VerificationToken verificationToken=verificationRepository.findByToken(verifyEmailRequest.getToken())
                .orElseThrow(()->new TokenException("Email Verification Token was invalid"));

        if(verificationToken.getTokenType()!=TokenType.EMAIL_VERIFICATION){
            throw new TokenException("Invalid Token Type");
        }
        if (verificationToken.getTokenExpiryDate().isBefore(LocalDateTime.now())){
            throw new TokenException("Email Verification Token was expired");
        }
        Employee employee=verificationToken.getEmployee();
        if (!employee.getEmployeeId().equals(employeeId)){
            throw new TokenException("Invalid Verification Token");
        }
        employee.setEmail(verificationToken.getNewEmail());

        verificationToken.setWasUsed(true);
        verificationToken.setTokenStatus(TokenStatus.VERIFIED);
        verificationRepository.save(verificationToken);

    }

    public String updatePhoneNumber(UUID employeeId, PhoneNumberUpdateRequest phoneNumberUpdateRequest) {

        if (employeeRepository.existsByPhoneNumber(phoneNumberUpdateRequest.getPhoneNumber())){
            throw new EmployeeException("Phone Number already registered by another user");
        }

        Employee employee = employeeRepository.findByIdAndIsActive(employeeId).orElseThrow( ()->new EmployeeException("Employee not found or inactive"));

        String token=utilityService.generateNumericCode(6);

        VerificationToken verificationToken = setVerificationToken(token, employee,TokenType.PHONE_VERIFICATION);
        verificationToken.setNewPhonenumber(phoneNumberUpdateRequest.getPhoneNumber());
        verificationRepository.save(verificationToken);

        kafkaService.sendSmsVerification(phoneNumberUpdateRequest.getPhoneNumber(),token,employee.getEmployeeName()).thenApply(status->{
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

    private static VerificationToken setVerificationToken(String token, Employee employee,TokenType tokenType) {
        VerificationToken verificationToken=new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setEmployee(employee);
        verificationToken.setTokenStatus(TokenStatus.PENDING);
        verificationToken.setTokenType(tokenType);
        verificationToken.setTokenExpiryDate(LocalDateTime.now().plusMinutes(10));
        verificationToken.setCreatedAt(LocalDateTime.now());
        return verificationToken;
    }

    public void verifyPhoneNumber(UUID employeeId, VerifyTokenRequest verifyPhoneNumberRequest) {

        VerificationToken verificationToken=verificationRepository.findByToken(verifyPhoneNumberRequest.getToken())
                .orElseThrow(()->new TokenException("Verification Token was invalid"));

        if(verificationToken.getTokenType()!=TokenType.PHONE_VERIFICATION){
            throw new TokenException("Invalid Token Type");
        }
        if (verificationToken.getTokenExpiryDate().isBefore(LocalDateTime.now())){
            throw new TokenException("Verification Token was expired");
        }
        Employee employee=verificationToken.getEmployee();
        if (!employee.getEmployeeId().equals(employeeId)){
            throw new TokenException("Invalid Verification Token");
        }
        employee.setPhoneNumber(verificationToken.getNewPhonenumber());

        verificationToken.setWasUsed(true);
        verificationToken.setTokenStatus(TokenStatus.VERIFIED);
        verificationRepository.save(verificationToken);
    }

    public String forgetPassword(UUID employeeId, String typeOfVerification) {
        log.info("Initiating forget password process for employeeId: {}", employeeId);
        Employee registedEmployee= employeeRepository.findByIdAndIsActive(employeeId)
                .orElseThrow(()->new EmployeeException("Employee not found or inactive"));

        String token = utilityService.generateNumericCode(6);
        VerificationToken verificationToken = setVerificationToken(token,registedEmployee,TokenType.FORGET_PASSWORD);

        log.info("Generated verification token for employeeId: {}", employeeId);
        verificationRepository.save(verificationToken);

        if (typeOfVerification.equalsIgnoreCase("Email")){
            kafkaService.sendEmailVerification(registedEmployee.getEmail(),token,registedEmployee.getEmployeeName(),"forget-password").thenApply(status->{
                if (!status){
                    verificationToken.setTokenStatus(TokenStatus.FAILED);
                    throw new NotificationException("Failed to send Email");
                }else {
                    verificationToken.setTokenStatus(TokenStatus.SENT);
                }
                return verificationRepository.save(verificationToken);
            });
            log.info("Sent forget password email to employeeId: {}", employeeId);
        }else {
            if (registedEmployee.getPhoneNumber()==null){
                throw new EmployeeException("Employee does not have a phone number associated");
            }
            CompletableFuture<Boolean> smsStatus = kafkaService.sendSmsVerification(registedEmployee.getPhoneNumber(), token, registedEmployee.getEmployeeName());
            smsStatus.thenApply(status->{
                if (!status){
                    verificationToken.setTokenStatus(TokenStatus.FAILED);
                    throw new NotificationException("Failed to send SMS");
                }else {
                    verificationToken.setTokenStatus(TokenStatus.SENT);
                }
                return verificationRepository.save(verificationToken);
            });
            log.info("Sent forget password SMS to employeeId: {}", employeeId);
        }

        return "Verification code sent via " + typeOfVerification.toLowerCase();
    }

    public String verifyForgetPassword(UUID employeeId, PasswordUpdateRequest passwordChangeRequest) {

        log.info("Verifying forget password token for employeeId: {}", employeeId);
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())){
            throw new EmployeeException("New Password and Confirm Password do not match");
        }

        VerificationToken verificationToken=verificationRepository.findByToken(passwordChangeRequest.getToken())
                .orElseThrow(()->new TokenException("Verification Token was invalid"));

        if(verificationToken.getTokenType()!=TokenType.FORGET_PASSWORD){
            throw new TokenException("Invalid Token Type");
        }
        if (verificationToken.getTokenExpiryDate().isBefore(LocalDateTime.now())){
            throw new TokenException("Verification Token was expired");
        }
        Employee employee=verificationToken.getEmployee();
        if (!employee.getEmployeeId().equals(employeeId)){
            throw new EmployeeException("Invalid Verification Token for the user");
        }

        // need to encode the password
        employee.setPassword(passwordChangeRequest.getNewPassword());

        verificationToken.setWasUsed(true);
        verificationToken.setTokenStatus(TokenStatus.VERIFIED);
        verificationRepository.save(verificationToken);
        log.info("Password updated successfully for employeeId: {}", employeeId);
        return "Password updated successfully";

    }
}
