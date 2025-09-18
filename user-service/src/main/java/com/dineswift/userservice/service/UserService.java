package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.BookingException;
import com.dineswift.userservice.exception.CustomAuthenticationException;
import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.model.entites.*;
import com.dineswift.userservice.model.request.LoginRequest;
import com.dineswift.userservice.model.request.UserDetailsRequest;
import com.dineswift.userservice.model.request.UserRequest;
import com.dineswift.userservice.model.response.AuthResponse;
import com.dineswift.userservice.model.response.BookingDTO;
import com.dineswift.userservice.model.response.UserDTO;
import com.dineswift.userservice.repository.BookingRepository;
import com.dineswift.userservice.repository.RoleRepository;
import com.dineswift.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.*;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;


    public UserService(UserRepository userRepository, BookingRepository bookingRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.modelMapper = modelMapper;
    }

    public UserDTO updateDetails(UserDetailsRequest userDetailsRequest, UUID userId) {

        User user=userRepository.findById(userId).orElseThrow(
                () -> new UserException("User not found with ID: " + userId));

        if (!isValidUser(user)){
            throw new CustomAuthenticationException("Not a Valid User");
        }
        updateUserFromRequest(user, userDetailsRequest);

        userRepository.save(user);

        return modelMapper.map(user,UserDTO.class);
    }

    private boolean isValidUser(User user) {

        User currentUser=getCurrentUser();
        if (user.equals(currentUser)){
            return true;
        }
        return false;
    }

    private User getCurrentUser() {
        try {
            User user = null;
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            if (authentication!=null && authentication.isAuthenticated()){
                Object principal = authentication.getPrincipal();

                if (principal instanceof User) {
                     user=(User) principal;
                }
            }
            return user;
        } catch (AuthenticationException e) {
            throw new CustomAuthenticationException(e.getLocalizedMessage());
        }

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

        user.setIsActive(false);
        userRepository.save(user);
    }
}
