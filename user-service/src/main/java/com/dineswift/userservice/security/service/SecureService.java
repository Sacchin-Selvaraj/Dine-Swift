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

    public boolean isValidUser(User user) {

        if (user==null)
            return true;

        User currentUser=getCurrentUser();

        if (currentUser==null)
            return true;

        if (user.getUserId().equals(currentUser.getUserId())){
            return false;
        }
        return true;
    }

    public boolean isValidUser(UUID userId) {

        if (userId==null)
            return true;

        User currentUser=getCurrentUser();

        if (currentUser==null)
            return true;

        if (userId.equals(currentUser.getUserId())){
            return false;
        }
        return true;
    }

    private User getCurrentUser() {
        try {
            User user = null;
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if (authentication!=null && authentication.isAuthenticated()){
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
            }
            return user;
        } catch (AuthenticationException e) {
            throw new CustomAuthenticationException(e.getLocalizedMessage());
        }

    }

}
