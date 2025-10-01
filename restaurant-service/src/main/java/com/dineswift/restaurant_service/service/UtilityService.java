package com.dineswift.restaurant_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UtilityService {

    private static final SecureRandom random = new SecureRandom();

    public String generateNumericCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // 0–9
        }
        return sb.toString();
    }
}
