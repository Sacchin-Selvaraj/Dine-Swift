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
import com.dineswift.restaurant_service.payload.request.verification.ForgotPasswordRequest;
import com.dineswift.restaurant_service.repository.EmployeeRepository;
import com.dineswift.restaurant_service.repository.VerificationRepository;
import com.dineswift.restaurant_service.security.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final VerificationRepository verificationRepository;
    private final KafkaService kafkaService;
    private final EmployeeRepository employeeRepository;
    private final UtilityService utilityService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;


    public void updateEmail(EmailUpdateRequest emailUpdateRequest) {

        UUID employeeId = authService.getAuthenticatedId();

        if (employeeRepository.existsByEmail(emailUpdateRequest.getEmail())){
            log.error("Email {} already registered by another user", emailUpdateRequest.getEmail());
            throw new EmployeeException("Email already registered by another user");
        }
        Employee employee = employeeRepository.findByIdAndIsActiveWoEntity(employeeId)
                .orElseThrow(()->new EmployeeException("Employee not found or inactive"));

        log.info("Generating email verification token for employeeId: {}", employeeId);
        String token=utilityService.generateNumericCode(6);

        VerificationToken verificationToken=setVerificationToken(token,employee, TokenType.EMAIL_VERIFICATION);
        verificationToken.setNewEmail(emailUpdateRequest.getEmail());

        verificationRepository.save(verificationToken);

        log.info("Sending email verification to {} for employeeId: {}", emailUpdateRequest.getEmail(), employeeId);
        kafkaService.sendEmailVerification(emailUpdateRequest.getEmail(),token,employee.getEmployeeName(),"email-verification").thenApply(status->{
            if (!status){
                verificationToken.setTokenStatus(TokenStatus.FAILED);
                throw new NotificationException("Failed to send Email");
            }else {
                verificationToken.setTokenStatus(TokenStatus.SENT);
            }
            return verificationRepository.save(verificationToken);
        });

    }


    public void verifyEmail( VerifyTokenRequest verifyEmailRequest) {
        UUID employeeId = authService.getAuthenticatedId();

        log.info("Verifying email token for employeeId: {}", employeeId);
        VerificationToken verificationToken = verifyTokenExistence(verifyEmailRequest.getToken(),TokenType.EMAIL_VERIFICATION);

        Employee employee = verificationToken.getEmployee();
        employee.setEmail(verificationToken.getNewEmail());

        if (!employeeId.equals(employee.getEmployeeId())){
            throw new EmployeeException("Verification Token is not from valid Employee");
        }
        verificationToken.setWasUsed(true);
        verificationToken.setTokenStatus(TokenStatus.VERIFIED);

        verificationRepository.save(verificationToken);

    }

    public void updatePhoneNumber(PhoneNumberUpdateRequest phoneNumberUpdateRequest) {

        UUID employeeId = authService.getAuthenticatedId();

        if (employeeRepository.existsByPhoneNumber(phoneNumberUpdateRequest.getPhoneNumber())){
            log.error("Phone Number {} already registered by another user", phoneNumberUpdateRequest.getPhoneNumber());
            throw new EmployeeException("Phone Number already registered by another user");
        }

        Employee employee = employeeRepository.findByIdAndIsActiveWoEntity(employeeId)
                .orElseThrow(()->new EmployeeException("Employee not found or inactive"));

        log.info("Generating phone number verification token for employeeId: {}", employeeId);
        String token=utilityService.generateNumericCode(6);

        VerificationToken verificationToken = setVerificationToken(token, employee,TokenType.PHONE_VERIFICATION);
        verificationToken.setNewPhonenumber(phoneNumberUpdateRequest.getPhoneNumber());

        verificationRepository.save(verificationToken);

        kafkaService.sendSmsVerification(phoneNumberUpdateRequest.getPhoneNumber(),token,employee.getEmployeeName()).thenApply(status->{
            if (!status){
                verificationToken.setTokenStatus(TokenStatus.FAILED);
                log.error("Failed to send SMS to {} for employeeId: {}", phoneNumberUpdateRequest.getPhoneNumber(), employeeId);
                throw new NotificationException("Failed to send SMS");
            }else {
                verificationToken.setTokenStatus(TokenStatus.SENT);
            }
            return verificationRepository.save(verificationToken);
        });
    }

    private static VerificationToken setVerificationToken(String token, Employee employee,TokenType tokenType) {
        log.info("Setting verification token for employee");
        VerificationToken verificationToken=new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setEmployee(employee);
        verificationToken.setTokenStatus(TokenStatus.PENDING);
        verificationToken.setTokenType(tokenType);
        verificationToken.setTokenExpiryDate(LocalDateTime.now().plusMinutes(10));
        verificationToken.setCreatedAt(LocalDateTime.now());
        return verificationToken;
    }

    public void verifyPhoneNumber( VerifyTokenRequest verifyPhoneNumberRequest) {

        UUID employeeId = authService.getAuthenticatedId();
        VerificationToken verificationToken = verifyTokenExistence(verifyPhoneNumberRequest.getToken(),TokenType.PHONE_VERIFICATION);

        Employee employee = verificationToken.getEmployee();
        log.info("Updating phone number for employeeId: {}", employeeId);
        employee.setPhoneNumber(verificationToken.getNewPhonenumber());

        if (!employeeId.equals(employee.getEmployeeId())){
            throw new EmployeeException("Verification Token is not from valid Employee");
        }

        verificationToken.setWasUsed(true);
        verificationToken.setTokenStatus(TokenStatus.VERIFIED);

        verificationRepository.save(verificationToken);
    }

    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {

        String typeOfVerification = forgotPasswordRequest.getTypeOfVerification();
        log.info("Initiating forgot password process for employeeInput: {}", forgotPasswordRequest.getEmployeeInput());
        Employee registedEmployee;

        if (typeOfVerification.equalsIgnoreCase("Email")){
            String employeeEmail = forgotPasswordRequest.getEmployeeInput();
            registedEmployee=employeeRepository.findByEmailAndIsActive(employeeEmail)
                    .orElseThrow(()->new EmployeeException("Employee not found or inactive"));
        }else {
            String phoneNumber = forgotPasswordRequest.getEmployeeInput();
            registedEmployee=employeeRepository.findByPhoneNumberAndIsActive(phoneNumber)
                    .orElseThrow(()->new EmployeeException("Employee not found or inactive"));
        }

        String token = utilityService.generateNumericCode(6);
        VerificationToken verificationToken = setVerificationToken(token,registedEmployee,TokenType.FORGOT_PASSWORD);

        log.info("Generated verification token for employee");
        verificationRepository.save(verificationToken);

        if (typeOfVerification.equalsIgnoreCase("Email")){
            kafkaService.sendEmailVerification(registedEmployee.getEmail(),token,registedEmployee.getEmployeeName(),"forget-password").thenApply(status->{
                if (!status){
                    verificationToken.setTokenStatus(TokenStatus.FAILED);
                    log.error("Failed to send forgot password email to {}", registedEmployee.getEmail());
                    throw new NotificationException("Failed to send Email");
                }else {
                    verificationToken.setTokenStatus(TokenStatus.SENT);
                }
                return verificationRepository.save(verificationToken);
            });
            log.info("Sent forgot password email to employee");
        }else {
            if (registedEmployee.getPhoneNumber()==null){
                throw new EmployeeException("Employee does not have a phone number associated");
            }
            CompletableFuture<Boolean> smsStatus = kafkaService.sendSmsVerification(registedEmployee.getPhoneNumber(), token, registedEmployee.getEmployeeName());
            smsStatus.thenApply(status->{
                if (!status){
                    verificationToken.setTokenStatus(TokenStatus.FAILED);
                    log.error("Failed to send forgot password SMS to {}", registedEmployee.getPhoneNumber());
                    throw new NotificationException("Failed to send SMS");
                }else {
                    verificationToken.setTokenStatus(TokenStatus.SENT);
                }
                return verificationRepository.save(verificationToken);
            });
            log.info("Sent forgot password SMS to employee");
        }

    }

    public String verifyForgotPassword(PasswordUpdateRequest passwordChangeRequest) {

        log.info("Verifying forgot password token");
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())){
            throw new EmployeeException("New Password and Confirm Password do not match");
        }

        VerificationToken verificationToken = verifyTokenExistence(passwordChangeRequest.getToken(),TokenType.FORGOT_PASSWORD);

        Employee employee = verificationToken.getEmployee();
        employee.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));

        verificationToken.setWasUsed(true);
        verificationToken.setTokenStatus(TokenStatus.VERIFIED);
        verificationRepository.save(verificationToken);
        return "Password updated successfully";

    }

    public VerificationToken verifyTokenExistence(String verifyToken,TokenType tokenType) {
        log.info("Verifying existence of token: {}", verifyToken);

        VerificationToken verificationToken=verificationRepository.findByToken(verifyToken)
                .orElseThrow(()->new TokenException("Verification Token was invalid"));

        if(verificationToken.getTokenType()!=tokenType){
            throw new TokenException("Invalid Token Type");
        }
        if (verificationToken.getTokenExpiryDate().isBefore(LocalDateTime.now())){
            throw new TokenException("Verification Token was expired");
        }
        return verificationToken;
    }
}
