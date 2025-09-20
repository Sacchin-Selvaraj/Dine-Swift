package com.dineswift.userservice.security.service;

import com.dineswift.userservice.exception.CustomAuthenticationException;
import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.Cart;
import com.dineswift.userservice.model.entites.Role;
import com.dineswift.userservice.model.entites.User;
import com.dineswift.userservice.model.request.LoginRequest;
import com.dineswift.userservice.model.request.UserRequest;
import com.dineswift.userservice.model.response.AuthResponse;
import com.dineswift.userservice.model.response.UserDTO;
import com.dineswift.userservice.repository.RoleRepository;
import com.dineswift.userservice.repository.UserRepository;
import com.dineswift.userservice.security.utilities.JWTUtilities;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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
    private final JWTUtilities jwtUtilities;

    public AuthService(ModelMapper modelMapper, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JWTUtilities jwtUtilities) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtilities = jwtUtilities;
    }

    public AuthResponse signupUser(UserRequest userRequest) {
        try {
            verifyUser(userRequest);

            User user=modelMapper.map(userRequest,User.class);
            String encodedPassword=passwordEncoder.encode(userRequest.getPassword());
            user.setPassword(encodedPassword);

            Cart cart=new Cart();

            Role role=roleRepository.findByRoleName("USER").orElseThrow(
                    ()-> new UserException("Role Not Found")
            );

            user.setRoles(Set.of(role));
            user.setCart(cart);

            userRepository.save(user);

            UserDTO userDTO=modelMapper.map(user,UserDTO.class);

            AuthResponse authResponse=new AuthResponse();
            authResponse.setUser(userDTO);
            authResponse.setExpiresIn(5);
            authResponse.setAuthToken(jwtUtilities.generateToken(userDTO.getEmail()));

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
            throw new RuntimeException("Username already taken!");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }
    }

    public AuthResponse signInUser(LoginRequest loginRequest) {

        String jwtToken;
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),loginRequest.getPassword()
            ));

            jwtToken=jwtUtilities.generateToken(loginRequest.getEmail());

            User user=userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()->new UserException(" User not found with provided mail Id"));
            isActiveUser(user);

            UserDTO userDTO=modelMapper.map(user,UserDTO.class);

            AuthResponse authResponse=new AuthResponse();
            authResponse.setUser(userDTO);
            authResponse.setExpiresIn(15);
            authResponse.setAuthToken(jwtToken);
            return authResponse;
        } catch (AuthenticationException e) {
            throw new CustomAuthenticationException(e.getLocalizedMessage());
        }
    }

    private void isActiveUser(User user) {

        if (!user.getIsActive()){
            throw new UserException("User account was inactive");
        }
    }
}
