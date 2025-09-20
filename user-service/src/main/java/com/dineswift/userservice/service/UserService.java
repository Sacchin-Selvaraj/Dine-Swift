package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.CustomAuthenticationException;
import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.*;
import com.dineswift.userservice.model.request.PasswordUpdateRequest;
import com.dineswift.userservice.model.request.UserDetailsRequest;
import com.dineswift.userservice.model.request.UsernameUpdateRequest;
import com.dineswift.userservice.model.response.BookingDTO;
import com.dineswift.userservice.model.response.UserDTO;
import com.dineswift.userservice.repository.BookingRepository;
import com.dineswift.userservice.repository.UserRepository;
import com.dineswift.userservice.security.service.SecureService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;
    private final SecureService secureService;
    private final PasswordEncoder passwordEncoder;



    public UserService(UserRepository userRepository, BookingRepository bookingRepository, ModelMapper modelMapper, SecureService secureService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.modelMapper = modelMapper;
        this.secureService = secureService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO updateDetails(UserDetailsRequest userDetailsRequest, UUID userId) {

        User user=userRepository.findById(userId).orElseThrow(
                () -> new UserException("User not found with ID: " + userId));

        if (secureService.isValidUser(user)){
            throw new CustomAuthenticationException("Not a Valid User");
        }
        updateUserFromRequest(user, userDetailsRequest);

        userRepository.save(user);

        return modelMapper.map(user,UserDTO.class);
    }

    private void updateUserFromRequest(User user, UserDetailsRequest request) {
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getDob() != null) user.setDob(request.getDob());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getArea() != null) user.setArea(request.getArea());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getDistrict() != null) user.setDistrict(request.getDistrict());
        if (request.getState() != null) user.setState(request.getState());
        if (request.getCountry() != null) user.setCountry(request.getCountry());
        if (request.getPincode() != null) user.setPincode(request.getPincode());
    }


    public Page<BookingDTO> getBookings(UUID userId, Integer pageNumber, Integer limit, BookingStatus bookingStatus, String sortField, String sortOrder) {

        if (secureService.isValidUser(userId)){
            throw new CustomAuthenticationException("Not a Valid User");
        }
        Set<String> allowedFields = Set.of("createdAt", "lastModifiedAt", "bookingStatus","tableBookingId","bookingTime");

        if (!allowedFields.contains(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }

        Sort sort=sortOrder.equalsIgnoreCase("asc")?Sort.by(sortField).ascending():Sort.by(sortField).descending();

        Pageable pageable= PageRequest.of(pageNumber,limit,sort);

        Page<Booking> bookings;
        if (bookingStatus==null){
            bookings=bookingRepository.findByUser_UserId(userId,pageable);
        }else {
            bookings=bookingRepository.findByUser_UserIdAndBookingStatus(userId,bookingStatus,pageable);
        }
//        if (bookings.isEmpty()){
//            throw new BookingException("No Bookings found with given condition");
//        }

        return bookings.map(booking -> modelMapper.map(booking,BookingDTO.class));
    }

    public void deactivateUser(UUID userId) {
        User user=userRepository.findById(userId).orElseThrow(
                () -> new UserException("User not found with ID: " + userId));

        if (secureService.isValidUser(user)){
            throw new CustomAuthenticationException("Not a Valid User");
        }
        user.setIsActive(false);
        userRepository.save(user);
    }

    public void updateUsername(UUID userId, UsernameUpdateRequest usernameRequest) {

        if (secureService.isValidUser(userId)){
            throw new CustomAuthenticationException("Not a Valid User");
        }
        validUsername(usernameRequest.getUsername(),userId);
        Optional<User> user=userRepository.findById(userId);
        user.ifPresent(
                currentUser -> currentUser.setUsername(usernameRequest.getUsername())
        );
        userRepository.save(user.get());
    }

    private void validUsername(String username, UUID userId) {
        userRepository.findByUsername(username).ifPresent(
                user -> {
                    if (!user.getUserId().equals(userId)) {
                        throw new UserException("Username was already taken");
                    }
                });
    }

    public void updatePassword(UUID userId, PasswordUpdateRequest passwordRequest) {
        if (secureService.isValidUser(userId)){
            throw new CustomAuthenticationException("Not a Valid User");
        }

        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())){
            throw new UserException("Password Mismatch between new and confirm password");
        }
        User user=userRepository.findById(userId).orElseThrow(()->new UserException("User not found with provided Id"));

        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), user.getPassword())){
            throw new UserException("Old Password is Invalid");
        }
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));

        userRepository.save(user);
    }


}
