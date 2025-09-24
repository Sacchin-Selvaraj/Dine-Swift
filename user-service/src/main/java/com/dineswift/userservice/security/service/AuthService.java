package com.dineswift.userservice.security.service;

import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.Cart;
import com.dineswift.userservice.model.entites.Role;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.model.request.UserRequest;
import com.dineswift.userservice.model.response.AuthResponse;
import com.dineswift.userservice.model.response.UserDTO;
import com.dineswift.userservice.repository.RoleRepository;
import com.dineswift.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Transactional
public class AuthService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(ModelMapper modelMapper, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse signupUser(UserRequest userRequest) {
        try {
            verifyUser(userRequest);

            User user=modelMapper.map(userRequest,User.class);

            Cart cart=new Cart();

            Role role=roleRepository.findByRoleName("USER").orElseThrow(
                    ()-> new UserException("Role Not Found")
            );

            user.setRoles(Set.of(role));
            user.setCart(cart);

            userRepository.save(user);

            UserDTO userDTO=modelMapper.map(user,UserDTO.class);


            return authResponse;

        } catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException(e.getLocalizedMessage());
        }

    }

    private void verifyUser(UserRequest userRequest) {
        if (userRequest ==null){
            throw new UserException("User Details not found");
        }
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new UserException("Username already taken!");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserException("Email already registered!");
        }
    }


    private void isActiveUser(User user) {

        if (!user.getIsActive()){
            throw new UserException("User account was inactive");
        }
    }
}
