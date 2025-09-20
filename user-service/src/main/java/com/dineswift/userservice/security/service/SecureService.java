package com.dineswift.userservice.security.service;

import com.dineswift.userservice.exception.CustomAuthenticationException;
import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecureService {

    private final UserRepository userRepository;

    public SecureService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void isValidUser(UUID userId) {

        if (userId==null)
            throw new CustomAuthenticationException("Not a Valid UserId");

        User currentUser=getCurrentUser();

        if (currentUser==null)
            throw new CustomAuthenticationException("No User found with given token");

        if (!userId.equals(currentUser.getUserId())){
            throw new CustomAuthenticationException("Not a Valid User");
        }
    }

    private User getCurrentUser() {
        try {
            User user = null;
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new CustomAuthenticationException("User not authenticated");
            }
                Object principal = authentication.getPrincipal();

                if (principal instanceof User) {
                    user=(User) principal;
                } else {
                    String email=null;
                    if (principal instanceof UserDetails)
                        email=((UserDetails) principal).getUsername();
                    if (email!=null){
                        user=userRepository.findByEmail(email).orElseThrow(()->new UserException("Email not valid"));
                    }
                }
            return user;
        } catch (AuthenticationException e) {
            throw new CustomAuthenticationException(e.getLocalizedMessage());
        }

    }

}
