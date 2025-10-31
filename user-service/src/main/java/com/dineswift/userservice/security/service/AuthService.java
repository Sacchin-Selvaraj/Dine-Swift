package com.dineswift.userservice.security.service;


import com.dineswift.userservice.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    public UUID getAuthenticatedUserId() {
        log.info("Fetching authenticated user ID from security context");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String UUIDString=null;
        if (authentication!=null && authentication.isAuthenticated()){
             UUIDString = authentication.getPrincipal().toString();
        }else {
            log.error("No authenticated user found in security context");
            throw new UserException("User is not authenticated");
        }
        assert UUIDString != null;
        return UUID.fromString(UUIDString);
    }
}
