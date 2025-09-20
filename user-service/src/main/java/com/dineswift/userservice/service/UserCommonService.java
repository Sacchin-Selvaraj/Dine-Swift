package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@Service
public class UserCommonService {

    private final UserRepository userRepository;
    private static final SecureRandom random = new SecureRandom();

    public UserCommonService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findValidUser(UUID userId){

        User user=userRepository.findById(userId).orElseThrow(
                () -> new UserException("User not found with ID: " + userId));

        return user;
    }

    public String generateNumericCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // 0–9
        }
        return sb.toString();
    }
}
