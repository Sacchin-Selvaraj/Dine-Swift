package com.dineswift.Api_Auth.Service.utilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtilities {

    public boolean validateJwtToken(String authToken) {
        // Implement JWT validation logic here
        log.info("Validating JWT token: {}", authToken);
        // For demonstration purposes, we'll assume all tokens are valid
        return true;
    }
}
