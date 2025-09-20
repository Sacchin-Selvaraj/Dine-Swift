package com.dineswift.userservice.security.service;

import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.repository.UserRepository;
import com.dineswift.userservice.security.model.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user=userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("User not found with provided Mail id :" + email));
        return new CustomUserDetails(user);
    }
}
