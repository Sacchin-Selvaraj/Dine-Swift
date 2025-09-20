package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.CustomAuthenticationException;
import com.dineswift.userservice.model.request.EmailUpdateRequest;
import com.dineswift.userservice.security.service.SecureService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VerificationService {

    private final SecureService secureService;

    public VerificationService(SecureService secureService) {
        this.secureService = secureService;
    }


    public void updateEmail(UUID userId, EmailUpdateRequest emailUpdateRequest) {

        if (secureService.isValidUser(userId)){
            throw new CustomAuthenticationException("Not a Valid User");
        }

    }
}
