package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserCommonService {

    private final UserRepository userRepository;

    public UserCommonService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findValidUser(UUID userId){

        User user=userRepository.findById(userId).orElseThrow(
                () -> new UserException("User not found with ID: " + userId));

        return user;
    }
}
