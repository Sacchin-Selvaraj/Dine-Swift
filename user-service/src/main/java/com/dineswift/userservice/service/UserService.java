package com.dineswift.userservice.service;

import com.dineswift.userservice.exception.UserException;
import com.dineswift.userservice.mapper.UserMapper;
import com.dineswift.userservice.model.entites.*;
import com.dineswift.userservice.model.request.*;
import com.dineswift.userservice.model.response.BookingDTO;
import com.dineswift.userservice.model.response.GuestInformationResponse;
import com.dineswift.userservice.model.response.UserDTO;
import com.dineswift.userservice.model.response.UserResponse;
import com.dineswift.userservice.repository.BookingRepository;
import com.dineswift.userservice.repository.RoleRepository;
import com.dineswift.userservice.repository.UserRepository;
import com.dineswift.userservice.security.service.AuthService;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserCommonService userCommonService;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final BookingSpecification bookingSpecification;
    private final AuthService authService;


    @CacheEvict(value = {"user:details", "user:info"},
            key = "@authService.getAuthenticatedUserId()")
    @Transactional
    public void updateDetails(UserDetailsRequest userDetailsRequest) {

        UUID userId=authService.getAuthenticatedUserId();
        log.info("Updating user detail for userId: {}", userId);
        User user=userCommonService.findValidUser(userId);

        updateUserFromRequest(user, userDetailsRequest);

        userRepository.save(user);
        log.info("User details updated successfully for userId: {}", userId);
        userMapper.toDTO(user);
    }

    private void updateUserFromRequest(User user, UserDetailsRequest request) {
        log.info("Updating user details for userId: {}", user.getUserId());
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

    @Cacheable(
            value = "booking:pages",
            key = "#filter.hashCode()",
            unless = "#result == null || #result.isEmpty()"
    )
    public CustomPageDto<BookingDTO> getBookings(BookingFilter filter) {
        log.info("Get Bookings from the UserService");

        UUID userId = authService.getAuthenticatedUserId();
        Set<String> allowedFields = Set.of("createdAt", "lastModifiedAt", "bookingStatus","tableBookingId","bookingDate");

        if (!allowedFields.contains(filter.sortField())) {
            throw new IllegalArgumentException("Invalid sorts field "+ filter.sortField());
        }
        log.info("Sorting Field: {}, Order: {}", filter.sortField(), filter.sortOrder());

        Sort sort=filter.sortOrder().equalsIgnoreCase("asc")?
                Sort.by(filter.sortField()).ascending():Sort.by(filter.sortField()).descending();

        Pageable pageable=PageRequest.of(filter.page(),filter.limit(),sort);

        Page<Booking> bookings;
        log.info("Building booking specification");
        Specification<Booking> spec = Specification.<Booking>allOf()
                .and(bookingSpecification.hasBookingStatus(filter.bookingStatus()))
                .and(bookingSpecification.hasBookingDate(filter.bookingDate()))
                .and(bookingSpecification.belongsToUser(userId));

        bookings = bookingRepository.findAll(spec, pageable);

        log.info("Fetched {} bookings", bookings.getNumberOfElements());

        Page<BookingDTO> bookingDTOS = bookings.map(booking -> modelMapper.map(booking,BookingDTO.class));

        return new CustomPageDto<>(bookingDTOS);
    }

    @CacheEvict(value = {"user:details", "user:info"},
            key = "@authService.getAuthenticatedUserId()")
    @Transactional
    public void deactivateUser() {
        log.info("Deactivating user account");
        UUID userId = authService.getAuthenticatedUserId();

        User user=userCommonService.findValidUser(userId);
        log.info("Deactivating user with ID: {}", userId);
        user.setIsActive(false);

        userRepository.save(user);
    }

    @CacheEvict(value = {"user:details", "user:info"},
            key = "@authService.getAuthenticatedUserId()")
    @Transactional
    public void updateUsername(UsernameUpdateRequest usernameRequest) {
        UUID userId=authService.getAuthenticatedUserId();
        log.info("Updating username for userId: {}", userId);
        validUsername(usernameRequest.getUsername(),userId);

        User user=userCommonService.findValidUser(userId);
        user.setUsername(usernameRequest.getUsername());

        userRepository.save(user);
    }

    private void validUsername(String username, UUID userId) {
        userRepository.findByUsername(username).ifPresent(
                user -> {
                    if (!user.getUserId().equals(userId)) {
                        throw new UserException("Username was already taken");
                    }
                });
    }


    private void verifyUser(UserRequest userRequest) {
        if (userRequest==null){
            throw new UserException("User Details not found");
        }
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new UserException("Username already taken!");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserException("Email already registered!");
        }
        if (userRepository.existsByPhoneNumber(userRequest.getPhoneNumber())){
            throw new UserException("Phone Number already registered!");
        }
    }

    @Transactional
    public void signUpUser(@Valid UserRequest userRequest) {

        try {
            verifyUser(userRequest);
            log.info("Creating new user account for username: {}", userRequest.getUsername());
            User user=userMapper.toEntity(userRequest);
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

            log.info("Initializing cart for new user: {}", userRequest.getUsername());
            Cart cart=new Cart();

            Role role=roleRepository.findByRoleName(RoleName.ROLE_USER)
                    .orElseThrow(()-> new UserException("Role Not Found"));

            user.setRoles(Set.of(role));
            user.setCart(cart);

            userRepository.save(user);
            log.info("User account created successfully for username: {}", userRequest.getUsername());

        } catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException(e.getLocalizedMessage());
        }

    }

    public UserResponse loginUser(@Valid LoginRequest loginRequest) {

        User user=userRepository.findByEmailAndIsActive(loginRequest.getEmail()).orElseThrow(
                ()-> new UserException("Invalid Email or Account is deactivated")
        );

        if (!passwordEncoder.matches(loginRequest.getPassword(),user.getPassword())){
            throw new UserException("Invalid Password was provided");
        }

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public void updatePassword(PasswordUpdateRequest passwordRequest) {

        UUID userId=authService.getAuthenticatedUserId();
        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())){
            throw new UserException("Password Mismatch between new and confirm password");
        }
        User user=userCommonService.findValidUser(userId);

        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), user.getPassword())){
            throw new UserException("Current Password is Invalid");
        }
        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));

        userRepository.save(user);
    }

    @Cacheable(
            value = "user:info",
            key = "#userId",
            unless = "#result == null"
    )
    public GuestInformationResponse getUserInfo(UUID userId) {
        log.info("Fetching user information for userId: {}", userId);
        User user=userCommonService.findValidUser(userId);

        log.info("Successfully retrieved user information for userId: {}", userId);
        return userMapper.toGuestInformationResponse(user);
    }


    @Cacheable(value = "user:details",
            key = "@authService.getAuthenticatedUserId()",
            unless = "#result == null")
    public UserDTO getCurrentUserInfo() {
        UUID userId=authService.getAuthenticatedUserId();
        log.info("Fetching current user information for userId: {}", userId);

        User user=userCommonService.findValidUser(userId);
        log.info("Successfully retrieved current user information for userId: {}", userId);

        return userMapper.toDTO(user);
    }
}
